package cn.disy920.slbot.bukkit.database;

import cn.disy920.slbot.database.Database;
import cn.disy920.slbot.error.BasicError;
import cn.disy920.slbot.error.CustomizeError;
import cn.disy920.slbot.error.ErrorPacket;
import cn.disy920.slbot.utils.UUIDTool;
import cn.disy920.slbot.utils.system.OSChecker;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static cn.disy920.slbot.Main.LOGGER;
import static cn.disy920.slbot.Main.PLUGIN_INSTANCE;

public class YamlDatabase implements Database {

    public static final File DATABASE = new File(PLUGIN_INSTANCE.getDataFolder(), "DataBase");  // 主目录
    public static final File OTHER_DATA = new File(DATABASE, "OtherData"); // 记录其他额外信息的数据库
    public static final File QQ_DATA = new File(DATABASE, "QQData"); // 记录QQ-玩家对应关系的目录

    private static final File WHITELIST = new File(DATABASE, "WhiteList/WhiteList.yml");  // 白名单文件
    private static final File PLAYER_DATA_FILE = new File(OTHER_DATA, "Data.yml");  // 玩家数据文件

    private static final ErrorPacket NO_ERROR_PACKET = ErrorPacket.create(BasicError.NONE, null);
    
    public static final YamlDatabase INSTANCE = new YamlDatabase();

    /**
     * 将指定ID加入白名单
     * @param playerID 玩家ID
     * @return 错误信息包，包含错误信息
     */
    @Override
    @NotNull
    public synchronized ErrorPacket addWhiteList(String playerID) {
        if (playerID == null){
            return ErrorPacket.create(BasicError.EMPTY_ID, null);
        }

        YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(WHITELIST);

        List<String> whitelistIDList = whitelistYaml.getStringList("WhiteList");
        if (whitelistIDList.contains(playerID)){
            return ErrorPacket.create(BasicError.ALREADY_BIND, null);
        }
        for (String ID : whitelistIDList){
            if (playerID.equalsIgnoreCase(ID)){
                return ErrorPacket.create(BasicError.SIMILAR_ID, "相近的ID为: " + ID);
            }
        }

        whitelistIDList.add(playerID);
        whitelistYaml.set("WhiteList", whitelistIDList);

        try {
            whitelistYaml.save(WHITELIST);
            LOGGER.info("成功将" + playerID + "加入白名单");
            return NO_ERROR_PACKET;
        }
        catch (Exception e) {
            LOGGER.error("添加" + playerID + "的白名单时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return ErrorPacket.create(BasicError.BIND_ERROR, null);
        }
    }


    /**
     * 将指定ID移出白名单
     * @param playerID 玩家ID
     * @return 错误信息包，包含错误信息
     */
    @Override
    @NotNull
    public synchronized ErrorPacket delWhiteList(String playerID) {
        YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(WHITELIST);

        List<String> whitelistIDList = whitelistYaml.getStringList("WhiteList");
        if (!whitelistIDList.contains(playerID)){
            return ErrorPacket.create(BasicError.NOT_EXISTED, null);
        }

        whitelistIDList.remove(playerID);
        whitelistYaml.set("WhiteList", whitelistIDList);

        try {
            whitelistYaml.save(WHITELIST);
            LOGGER.info("成功将" + playerID + "移出白名单");
            return NO_ERROR_PACKET;
        }
        catch (Exception e) {
            LOGGER.error("移除" + playerID + "的白名单时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return ErrorPacket.create(BasicError.CANCEL_BIND_ERROR, null);
        }
    }


    /**
     * 将指定ID移出白名单
     * @param playerIDList 多个玩家ID的数组
     * @return 错误信息包，包含错误信息
     */
    @Override
    public synchronized ErrorPacket delWhiteList(List<String> playerIDList) {
        if (playerIDList == null || playerIDList.size() == 0){
            return ErrorPacket.create(BasicError.EMPTY_ID, null);
        }

        YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(WHITELIST);

        List<String> whitelistIDList = whitelistYaml.getStringList("WhiteList");

        for (String playerID : playerIDList){
            if (!whitelistIDList.contains(playerID)){
                continue;
            }

            whitelistIDList.remove(playerID);
        }

        whitelistYaml.set("WhiteList", whitelistIDList);

        try {
            whitelistYaml.save(WHITELIST);
            LOGGER.info("成功将" + playerIDList + "移出白名单");
        }
        catch (Exception e) {
            LOGGER.error("移除" + playerIDList + "的白名单时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return ErrorPacket.create(BasicError.CANCEL_BIND_ERROR, null);
        }

        return NO_ERROR_PACKET;
    }


    /**
     * 判断一个人是否在白名单中
     * @param playerID 该玩家的id
     * @return 一个boolean值，表示玩家是否在白名单中
     */
    @Override
    public synchronized boolean isInWhiteList(String playerID) {
        YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(WHITELIST);

        List<String> whitelistIDList = whitelistYaml.getStringList("WhiteList");

        return whitelistIDList.contains(playerID);
    }


    /**
     * 获取玩家白名单列表
     * @return 包含所有白名单ID的列表
     */
    @Override
    @NotNull
    public List<String> getWhiteList() {
        YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(WHITELIST);
        return whitelistYaml.getStringList("WhiteList");
    }


    /**
     * 判断一个人是否通过了审核（同时可在数据库不存在时自动创建）
     * @param QQNumber 该玩家的QQ号
     * @return 一个boolean值，表示玩家是否通过了审核
     */
    @Override
    public boolean hadPassedTheExam(long QQNumber) {
        try {
            File playerDatabase = new File(QQ_DATA, (QQNumber + ".yml"));

            if (!playerDatabase.exists()) {
                playerDatabase.createNewFile();  // 数据库不存在的时候自动创建一个
                return false;
            }

            YamlConfiguration playerDatabaseYaml = YamlConfiguration.loadConfiguration(playerDatabase);
            return playerDatabaseYaml.getBoolean("Had_Allowed");
        }
        catch (Exception e) {
            LOGGER.error("检查" + QQNumber + "的审核情况时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 通过玩家审核的方法
     * @param QQNumber 玩家QQ号
     * @return 错误信息包，包含错误信息
     */
    @Override
    @NotNull
    public ErrorPacket passExam(long QQNumber) {
        try {
            File playerDatabase = new File(QQ_DATA, (QQNumber + ".yml"));

            if (!playerDatabase.exists()) {
                playerDatabase.createNewFile();  // 数据库不存在的时候自动创建一个
            }

            YamlConfiguration playerDatabaseYaml = YamlConfiguration.loadConfiguration(playerDatabase);

            if(playerDatabaseYaml.getBoolean("Had_Allowed")){
                return ErrorPacket.create(BasicError.ALREADY_PASSED, ("请检查你输入的QQ号" + QQNumber + "是否正确！"));
            }

            playerDatabaseYaml.set("Had_Allowed", true);

            playerDatabaseYaml.save(playerDatabase);

            return NO_ERROR_PACKET;
        }
        catch (Exception e) {
            LOGGER.error("通过" + QQNumber + "的审核时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return ErrorPacket.create(BasicError.PASS_ERROR, null);
        }
    }


    /**
     * 将某玩家纳入未过审的列表
     * @param QQNumber 玩家的QQ号
     * @return 错误信息包，包含错误信息
     */
    @Override
    @NotNull
    public ErrorPacket denyExam(long QQNumber) {
        try {
            File denyListFile = new File(OTHER_DATA, "DenyList.yml");

            if(!denyListFile.exists()){
                denyListFile.createNewFile();
            }

            YamlConfiguration denyListYaml = YamlConfiguration.loadConfiguration(denyListFile);

            List<Long> denyList = denyListYaml.getLongList("Deny_List");
            denyList.add(QQNumber);

            denyListYaml.set("Deny_List", denyList);

            denyListYaml.save(denyListFile);

            return NO_ERROR_PACKET;
        }
        catch (Exception e) {
            LOGGER.error("添加审核未通过成员失败！以下是错误的堆栈信息：");
            e.printStackTrace();
            return ErrorPacket.create(BasicError.DENY_ERROR, null);
        }
    }


    /**
     * 获取所有未过审的玩家
     * @return 一个包含所有未过审玩家QQ的列表
     */
    @NotNull
    public synchronized List<Long> getDenyList(){
        try{
            File denyListFile = new File(OTHER_DATA, "DenyList.yml");

            if(denyListFile.exists()){
                YamlConfiguration denyListYaml = YamlConfiguration.loadConfiguration(denyListFile);

                return denyListYaml.getLongList("Deny_List");
            }

            return new ArrayList<>();
        }
        catch (Exception e){
            LOGGER.error("查询审核未通过成员失败！以下是错误的堆栈信息：");
            e.printStackTrace();

            return new ArrayList<>();
        }
    }


    /**
     * 将某玩家从未过审的列表中删除
     * @param QQNumber 玩家的QQ号
     */
    public synchronized void delDenyList(long QQNumber){
        try{
            File denyListFile = new File(OTHER_DATA, "DenyList.yml");

            if(denyListFile.exists()){
                YamlConfiguration denyListYaml = YamlConfiguration.loadConfiguration(denyListFile);

                List<Long> denyList = denyListYaml.getLongList("Deny_List");
                denyList.remove(QQNumber);

                denyListYaml.set("Deny_List", denyList);

                denyListYaml.save(denyListFile);
            }
        }
        catch (Exception e){
            LOGGER.error("移除审核未通过成员失败！以下是错误的堆栈信息：");
            e.printStackTrace();
        }
    }


    /**
     * 撤销玩家审核的方法
     * @param QQNumber 玩家QQ号
     * @return 错误信息包，包含错误信息
     */
    @Override
    @NotNull
    public ErrorPacket withdrawExam(long QQNumber) {
        try {
            File playerDatabase = new File(QQ_DATA, (QQNumber + ".yml"));

            if (!playerDatabase.exists()) {
                playerDatabase.createNewFile();  // 数据库不存在的时候自动创建一个
            }


            YamlConfiguration playerDatabaseYaml = YamlConfiguration.loadConfiguration(playerDatabase);

            if (!playerDatabaseYaml.getBoolean("Had_Allowed")){
                return ErrorPacket.create(BasicError.NOT_PASS, ("请检查你输入的QQ号" + QQNumber + "是否正确！"));
            }

            playerDatabaseYaml.set("Had_Allowed", false);

            playerDatabaseYaml.save(playerDatabase);

            return NO_ERROR_PACKET;
        }
        catch (Exception e) {
            LOGGER.error("取消" + QQNumber + "的审核时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return ErrorPacket.create(BasicError.WITHDRAW_ERROR, null);
        }
    }


    /**
     * 添加管理员的方法
     * @param QQNumber 要添加为管理的QQ号
     * @return 错误信息包，包含错误信息
     */
    @Override
    @NotNull
    public ErrorPacket addAdmin(long QQNumber) {
        List<Long> adminList = PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ");

        if (adminList.contains(QQNumber)){
            return ErrorPacket.create(BasicError.ALREADY_ADMIN, null);
        }
        adminList.add(QQNumber);

        PLUGIN_INSTANCE.getConfig().set("Admins_QQ", adminList);
        PLUGIN_INSTANCE.saveConfig();
        PLUGIN_INSTANCE.reloadConfig();

        return NO_ERROR_PACKET;
    }


    /**
     * 移除管理员的方法
     * @param QQNumber 要移除管理的QQ号
     * @return 错误信息包，包含错误信息
     */
    @Override
    @NotNull
    public ErrorPacket delAdmin(long QQNumber) {
        List<Long> adminList = PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ");

        if (!adminList.contains(QQNumber)){
            return ErrorPacket.create(BasicError.NOT_ADMIN, null);
        }
        adminList.remove(QQNumber);

        PLUGIN_INSTANCE.getConfig().set("Admins_QQ", adminList);
        PLUGIN_INSTANCE.saveConfig();
        PLUGIN_INSTANCE.reloadConfig();

        return NO_ERROR_PACKET;
    }


    /**
     * 判断某QQ号是否为管理员
     * @param QQNumber 待判断的QQ号
     * @return 一个boolean值，表示该QQ号是否为管理员
     */
    @Override
    public boolean isAdmin(long QQNumber) {
        List<Long> adminList = PLUGIN_INSTANCE.getConfig().getLongList("Admins_QQ");

        return adminList.contains(QQNumber);
    }


    /**
     * 删除一个人全部的信息（包括QQ号对应关系与白名单）
     * @param QQNumber 要删除用户的QQ号
     * @return 错误信息包，包含错误信息
     */
    @Override
    @NotNull
    public ErrorPacket clearUserData(long QQNumber) {
        try {
            File personalDataFile = new File(QQ_DATA, (QQNumber + ".yml"));

            if(personalDataFile.exists()){
                YamlConfiguration personDataYaml = YamlConfiguration.loadConfiguration(personalDataFile);

                List<String> personalIDList = personDataYaml.getStringList("Bind_ID");
                delWhiteList(personalIDList);  // 清除掉TA所有的白名单数据

                for (String id : personalIDList){
                    setOnlineMode(id, null);
                }

                personalDataFile.delete();  // 然后就删掉这个文件
            }
        }
        catch (Exception e) {
            LOGGER.error("移除" + QQNumber + "的个人信息时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return ErrorPacket.create(BasicError.CLEAR_ERROR, null);
        }

        return NO_ERROR_PACKET;
    }


    /**
     * 同步白名单与绑定的ID，恢复数据
     */
    public synchronized void recoverAllData(){
        try {
            File[] personalDataFiles = QQ_DATA.listFiles();

            YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(WHITELIST);
            List<String> rawWhitelistIDList = whitelistYaml.getStringList("WhiteList");
            List<String> finalWhitelistIDList = new ArrayList<>();

            if(personalDataFiles != null){
                for(File f : personalDataFiles){
                    if(f.isFile()){
                        if(f.getName().contains(".yml")){
                            YamlConfiguration personDataYaml = YamlConfiguration.loadConfiguration(f);
                            List<String> personalIDList = personDataYaml.getStringList("Bind_ID");

                            rawWhitelistIDList.addAll(personalIDList);
                        }
                    }
                }
            }

            for(String playerID : rawWhitelistIDList){
                if(!finalWhitelistIDList.contains(playerID)){
                    finalWhitelistIDList.add(playerID);
                }
            }

            whitelistYaml.set("WhiteList", finalWhitelistIDList);
            whitelistYaml.save(WHITELIST);

        }
        catch (Exception e){
            LOGGER.error("同步ID时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
        }
    }


    /**
     * 为指定QQ添加ID绑定（同时将该ID纳入白名单中）
     * @param QQNumber 该ID对应的QQ号
     * @param playerID 待绑定的玩家ID
     * @param owner 绑定人QQ号
     * @return 错误信息包，包含错误信息
     */
    @NotNull
    public synchronized ErrorPacket addBindID(long QQNumber, String playerID, long owner){

        File IDFile = new File(QQ_DATA, (QQNumber + ".yml"));

        try {
            if(!IDFile.exists()){
                if(!IDFile.createNewFile()){
                    return ErrorPacket.create(BasicError.BIND_ERROR, "错误信息：数据库文件创建失败！");
                }
            }

            YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(IDFile);

            ErrorPacket bindStatus = addWhiteList(playerID);

            if (bindStatus.getError() == BasicError.NONE) {  // 判断添加白名部分是否成功
                bindStatus = insertID(whitelistYaml, QQNumber, playerID, owner);

                if (bindStatus.getError() == BasicError.NONE) {
                    whitelistYaml.set("size", whitelistYaml.getInt("size", 0) + 1);
                }
            }

            whitelistYaml.save(IDFile);

            return bindStatus;
        }
        catch (Exception e){
            LOGGER.error("绑定" + QQNumber + "的ID：" + playerID + "时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return ErrorPacket.create(BasicError.BIND_ERROR, null);
        }
    }


    public synchronized int getBindNum(long QQNumber) {
        File IDFile = new File(QQ_DATA, (QQNumber + ".yml"));

        try {
            if (!IDFile.exists()) {
                if (!IDFile.createNewFile()) {
                    return 0;
                }
            }

            YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(IDFile);

            return whitelistYaml.getInt("size", 0);
        }
        catch (Exception e) {
            return -1;
        }
    }


    public synchronized ErrorPacket insertID(YamlConfiguration whitelistYaml, long QQNumber, String playerID, long owner) {
        for (int i = 1; i < Short.MAX_VALUE; i++) {
            ErrorPacket status = insertID(whitelistYaml, QQNumber, playerID, i, owner);
            if (!(status == ErrorPacket.ALREADY_USED)) {
                return status;
            }
        }

        return ErrorPacket.ALREADY_USED;
    }


    public synchronized ErrorPacket insertID(YamlConfiguration whitelistYaml, long QQNumber, String playerID, int slot, long owner) {
        try {
            if (whitelistYaml.getString("Bind_ID." + slot + ".ID") != null) {
                return ErrorPacket.ALREADY_USED;
            }
            else {
                whitelistYaml.set("Bind_ID." + slot + ".ID", playerID);
                if (whitelistYaml.getString("Bind_ID." + slot + ".UUID") == null) {
                    whitelistYaml.set("Bind_ID." + slot + ".UUID", UUIDTool.getStarLightUUID(QQNumber, slot).toString());
                }
                whitelistYaml.set("Bind_ID." + slot + ".Owner", owner);
            }


            return ErrorPacket.NONE;
        }
        catch (Exception e){
            LOGGER.error("绑定" + QQNumber + "的ID：" + playerID + "时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return ErrorPacket.create(BasicError.BIND_ERROR, null);
        }
    }


    /**
     * 为指定QQ解除ID绑定（同时将该ID移出白名单）
     * @param QQNumber 该ID对应的QQ号
     * @param playerID 待绑定的玩家ID
     * @return 错误信息包，包含错误信息
     */
    @NotNull
    public synchronized ErrorPacket delBindID(long QQNumber, @NotNull String playerID){

        File IDFile = new File(QQ_DATA, (QQNumber + ".yml"));

        try{
            if(!IDFile.exists()){
                return ErrorPacket.create(BasicError.NEVER_BIND, null);
            }

            YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(IDFile);

            boolean isOwner = false;
            for(int slot = 1; slot < whitelistYaml.getInt("size", 0) + 1; slot++) {
                if (playerID.equals(whitelistYaml.getString("Bind_ID." + slot + ".ID"))) {
                    whitelistYaml.set("Bind_ID." + slot + ".ID", null);
                    whitelistYaml.set("Bind_ID." + slot + ".Owner", null);
                    isOwner = true;
                    whitelistYaml.set("size", whitelistYaml.getInt("size", 1) - 1);
                    break;
                }
            }

            if (!isOwner) {
                return ErrorPacket.create(BasicError.NOT_OWNER, null);
            }

            ErrorPacket delStatus = delWhiteList(playerID);
            if(delStatus.getError() == BasicError.NONE) {
                whitelistYaml.save(IDFile);
            }

            return delStatus;
        }
        catch (Exception e){
            LOGGER.error("删除" + QQNumber + "的ID：" + playerID + "时出错！以下是错误的堆栈信息：");
            e.printStackTrace();
            return ErrorPacket.create(BasicError.CANCEL_BIND_ERROR, null);
        }
    }


    /**
     * 查询一个人绑定过的ID
     * @param QQNumber 该玩家的QQ号
     * @return 包含其绑定过的所有ID的列表
     */
    @NotNull
    public synchronized List<String> checkBindID(long QQNumber){
        File IDFile = new File(QQ_DATA, (QQNumber + ".yml"));

        List<String> result = new ArrayList<>();

        if(!IDFile.exists()){
            return result;
        }

        YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(IDFile);

        for(int i = 1; i < whitelistYaml.getInt("size", 0) + 1; i++) {
            String playerID = whitelistYaml.getString("Bind_ID." + i + ".ID");
            if (playerID != null) {
                result.add(playerID);
            }
            else {
                i--;
            }
        }

        return result;
    }

    @NotNull
    public synchronized List<String> checkBindIDWithOwner(long QQNumber) {
        File IDFile = new File(QQ_DATA, (QQNumber + ".yml"));

        List<String> result = new ArrayList<>();

        if(!IDFile.exists()){
            return result;
        }

        YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(IDFile);

        for(int i = 1; i < whitelistYaml.getInt("size", 0) + 1; i++) {
            String playerID = whitelistYaml.getString("Bind_ID." + i + ".ID");
            long owner = whitelistYaml.getLong("Bind_ID." + i + ".Owner", -1);
            if (playerID != null) {
                if (owner == -1 || owner == QQNumber) {
                    result.add(playerID);
                }
                else {
                    result.add(String.format("%s (绑定者: %d)", playerID, owner));
                }
            }
            else {
                i--;
            }
        }

        return result;
    }


    /**
     * 强制删除一个ID（建议仅腐竹可用）
     * @param playerID 要强制删除的ID
     * @return 错误信息包，包含错误信息
     */
    @NotNull
    public synchronized ErrorPacket forceDelBindID(String playerID){
        File[] IDFilesArray = QQ_DATA.listFiles();

        if(IDFilesArray == null){
            return ErrorPacket.create(new CustomizeError("ID: \"" + playerID + "\"还没有被任何人绑定过哦！", 202), null);
        }

        for(File IDFile : IDFilesArray){
            if(IDFile.isFile()){
                try {
                    YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(IDFile);

                    boolean isOwner = false;
                    for(int slot = 1; slot < whitelistYaml.getInt("size", 0) + 1; slot++) {
                        String dataID = whitelistYaml.getString("Bind_ID." + slot + ".ID");
                        if (playerID.equals(dataID)) {
                            whitelistYaml.set("Bind_ID." + slot + ".ID", null);
                            whitelistYaml.set("Bind_ID." + slot + ".Owner", null);
                            isOwner = true;
                            whitelistYaml.set("size", whitelistYaml.getInt("size", 1) - 1);
                            break;
                        }
                        else if (dataID == null) {
                            slot--;
                        }
                    }

                    if (!isOwner) {
                        continue;
                    }

                    ErrorPacket delStatus = delWhiteList(playerID);
                    if(delStatus.getError() == BasicError.NONE) {
                        whitelistYaml.save(IDFile);
                    }

                    return delStatus;
                }
                catch (Exception e){
                    LOGGER.error("强制删除ID：" + playerID + "时出错！以下是错误的堆栈信息：");
                    e.printStackTrace();
                    return ErrorPacket.create(BasicError.CANCEL_BIND_ERROR, null);
                }
            }
        }

        return ErrorPacket.create(new CustomizeError("ID: \"" + playerID + "\"还没有被任何人绑定过哦！", 202), null);
    }


    /**
     * 反查一个ID的QQ拥有者（建议仅管理可用）
     * @param playerID 要强制删除的ID
     * @return 该玩家的QQ号，若没有任何人绑定该ID，则返回null
     */
    @Nullable
    public synchronized String findQQByID(String playerID){
        File[] IDFilesArray = QQ_DATA.listFiles();

        if(IDFilesArray == null){
            return null;
        }

        for(File IDFile : IDFilesArray){
            if(IDFile.isFile()){
                try {
                    YamlConfiguration whitelistYaml = YamlConfiguration.loadConfiguration(IDFile);

                    for(int slot = 1; slot < whitelistYaml.getInt("size", 0) + 1; slot++) {
                        String dataID = whitelistYaml.getString("Bind_ID." + slot + ".ID");
                        String uuid = whitelistYaml.getString("Bind_ID." + slot + ".UUID");
                        if (playerID.equals(dataID)) {
                            return IDFile.getName().replace(".yml", "");
                        }
                        else if(dataID == null && uuid != null) {
                            slot--;
                        }
                    }
                }
                catch (Exception e){
                    LOGGER.error("查找ID：" + playerID + "时出错！以下是错误的堆栈信息：");
                    e.printStackTrace();
                    return null;
                }
            }
        }

        return null;
    }


    /**
     * 读取配置文件中玩家的IP
     * @param playerName 待读取的玩家名
     * @return 配置文件中玩家的IP值
     */
    @NotNull
    public String getPlayerIP(String playerName){
        YamlConfiguration dataYaml = YamlConfiguration.loadConfiguration(PLAYER_DATA_FILE);

        return dataYaml.getString(playerName + ".IP", "");
    }


    /**
     * 将玩家的IP保存至配置文件
     * @param playerName 待设置的玩家名
     * @param playerIP 玩家IP值
     */
    public void setPlayerIP(String playerName, String playerIP){
        YamlConfiguration dataYaml = YamlConfiguration.loadConfiguration(PLAYER_DATA_FILE);

        dataYaml.set(playerName + ".IP", playerIP);
        try{
            dataYaml.save(PLAYER_DATA_FILE);
        }
        catch (Exception e){
            LOGGER.error("保存" + playerName + "的IP时出错，以下是堆栈信息：");
            e.printStackTrace();
        }
    }


    /**
     * 读取配置文件中玩家免密登录截止的时间戳值
     * @param playerName 待读取的玩家名
     * @return 截止的时间戳值
     */
    public long getNoPwdStamp(String playerName){
        YamlConfiguration dataYaml = YamlConfiguration.loadConfiguration(PLAYER_DATA_FILE);
        return dataYaml.getLong(playerName + ".No_Password_Stamp");
    }


    /**
     * 将玩家的IP保存至配置文件
     * @param playerName 待设置的玩家名
     * @param stamp 时间戳值
     */
    public void setNoPwdStamp(String playerName, long stamp){
        YamlConfiguration dataYaml = YamlConfiguration.loadConfiguration(PLAYER_DATA_FILE);

        dataYaml.set(playerName + ".No_Password_Stamp", stamp);
        try{
            dataYaml.save(PLAYER_DATA_FILE);
        }
        catch (Exception e){
            LOGGER.error("保存" + playerName + "的时间戳时出错，以下是堆栈信息：");
            e.printStackTrace();
        }
    }


    /**
     * 获取玩家是否处于正版认证状态
     * @param playerName 待获取的玩家名
     * @return 一个boolean值，表示是否处于正版认证状态
     */
    public boolean isOnlineMode(String playerName){
        if (PLUGIN_INSTANCE.getConfig().getBoolean("Enable_Online_Mode")){
            YamlConfiguration dataYaml = YamlConfiguration.loadConfiguration(PLAYER_DATA_FILE);
            return dataYaml.getBoolean(playerName + ".Is_Online_Mode");
        }

        return false;
    }


    /**
     * 设置玩家的正版认证状态
     * @param playerName 待设置的玩家名
     * @param onlineMode 待设置的认证状态，为null则清除该玩家的正版认证信息
     */
    public void setOnlineMode(String playerName, @Nullable Boolean onlineMode){

        if (PLUGIN_INSTANCE.getConfig().getBoolean("Enable_Online_Mode")) {
            YamlConfiguration dataYaml = YamlConfiguration.loadConfiguration(PLAYER_DATA_FILE);

            dataYaml.set(playerName + ".Is_Online_Mode", onlineMode);
            try {
                dataYaml.save(PLAYER_DATA_FILE);
            } catch (Exception e) {
                LOGGER.error("保存" + playerName + "的正版模式时出错，以下是堆栈信息：");
                e.printStackTrace();
            }
        }
    }


    /**
     * 初始化Yaml数据库
     */
    public void init(){
        if(!DATABASE.exists() && !DATABASE.isDirectory()){  // 如果没有DataBase文件夹
            if(!DATABASE.mkdirs()){  // 那就创建它
                LOGGER.error("主数据库创建失败，插件即将自动关闭！请进行检查！");
                PLUGIN_INSTANCE.getPluginLoader().disablePlugin(PLUGIN_INSTANCE);
            }
        }


        File whitelist = new File(DATABASE, "WhiteList");  // 保存白名单的文件夹

        if(!whitelist.exists() && !whitelist.isDirectory()){
            if(!whitelist.mkdirs()){
                LOGGER.error("白名单数据库创建失败，插件即将自动关闭！请进行检查！");
                PLUGIN_INSTANCE.getPluginLoader().disablePlugin(PLUGIN_INSTANCE);
            }
        }

        File whitelistYml = new File(whitelist, "WhiteList.yml");

        if(!whitelistYml.exists()){
            try {
                whitelistYml.createNewFile();

                YamlConfiguration whitelistConf = YamlConfiguration.loadConfiguration(whitelistYml);

                whitelistConf.set("WhiteList", new ArrayList<String>());  // 创建默认白名（空白名）
                whitelistConf.save(whitelistYml);
            }
            catch (IOException e){
                LOGGER.error("白名单数据库(YAML)创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                PLUGIN_INSTANCE.getPluginLoader().disablePlugin(PLUGIN_INSTANCE);
            }
        }


        File qqData = new File(DATABASE, "QQData");  // 保存QQ号与ID对应关系的文件夹

        if(!qqData.exists() && !qqData.isDirectory()){
            if(!qqData.mkdirs()){
                LOGGER.error("QQ信息数据库创建失败，插件即将自动关闭！请进行检查！");
                PLUGIN_INSTANCE.getPluginLoader().disablePlugin(PLUGIN_INSTANCE);
            }
        }


        File otherData = new File(DATABASE, "OtherData");

        if(!otherData.exists() && !otherData.isDirectory()){
            if(!otherData.mkdirs()){
                LOGGER.error("补充数据库创建失败，插件即将自动关闭！请进行检查！");
                PLUGIN_INSTANCE.getPluginLoader().disablePlugin(PLUGIN_INSTANCE);
            }
        }

        File otherDataYml = new File(otherData, "Data.yml");

        if(!otherDataYml.exists()){
            try {
                otherDataYml.createNewFile();
            }
            catch (IOException e){
                LOGGER.error("补充数据库(YAML)创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                PLUGIN_INSTANCE.getPluginLoader().disablePlugin(PLUGIN_INSTANCE);
            }
        }

        File denyListYml = new File(otherData, "DenyList.yml");
        if(!denyListYml.exists()){
            try {
                denyListYml.createNewFile();
            }
            catch (IOException e){
                LOGGER.error("审核未通过列表(YAML)创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                PLUGIN_INSTANCE.getPluginLoader().disablePlugin(PLUGIN_INSTANCE);
            }
        }


        File helpData = new File(DATABASE, "HelpFile");
        if(!helpData.exists() && !helpData.isDirectory()){
            if(!helpData.mkdirs()){
                LOGGER.error("帮助文档库创建失败，插件即将自动关闭！请进行检查！");
                PLUGIN_INSTANCE.getPluginLoader().disablePlugin(PLUGIN_INSTANCE);
            }
        }


        File helpDataFileNormal = new File(helpData, "player_help_msg.txt");

        if(!helpDataFileNormal.exists()){
            try {
                helpDataFileNormal.createNewFile();
            }
            catch (IOException e){
                LOGGER.error("玩家帮助文档创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                PLUGIN_INSTANCE.getPluginLoader().disablePlugin(PLUGIN_INSTANCE);
            }
        }

        File qAndADataFileNormal = new File(helpData, "player_q_and_a_msg.txt");

        if(!qAndADataFileNormal.exists()){
            try {
                qAndADataFileNormal.createNewFile();
            }
            catch (IOException e){
                LOGGER.error("玩家Q&A文档创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                PLUGIN_INSTANCE.getPluginLoader().disablePlugin(PLUGIN_INSTANCE);
            }
        }

        File helpDataFileAdmin = new File(helpData, "admin_help_msg.txt");

        if(!helpDataFileAdmin.exists()){
            try {
                helpDataFileAdmin.createNewFile();
            }
            catch (IOException e){
                LOGGER.error("玩家帮助文档创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                PLUGIN_INSTANCE.getPluginLoader().disablePlugin(PLUGIN_INSTANCE);
            }
        }

        File welcomeFile = new File(helpData, "welcome_msg.txt");

        if(!welcomeFile.exists()){
            try {
                welcomeFile.createNewFile();
            }
            catch (IOException e){
                LOGGER.error("欢迎新成员文档创建错误，插件即将自动关闭！以下是错误的堆栈信息：");
                e.printStackTrace();
                PLUGIN_INSTANCE.getPluginLoader().disablePlugin(PLUGIN_INSTANCE);
            }
        }

        changePermission(PLUGIN_INSTANCE.getDataFolder());
    }


    /**
     * 在Linux环境下，增加权限，使路径及子路径都有权限
     */
    public void changePermission(File root){
        try {
            if (OSChecker.isLinux()) {
                String cmdGrant = "chmod -R 777 " + root.getAbsolutePath();
                Runtime.getRuntime().exec(cmdGrant);
            }
        }
        catch (Exception e){
            LOGGER.warn("文件权限修改失败，这可能会导致您在修改和删除前，需要手动设置权限！");
            if(PLUGIN_INSTANCE.getConfig().getBoolean("Debug_Mode")){
                LOGGER.warn("以下是错误的堆栈信息:");
                e.printStackTrace();
            }
        }
    }
}
