package cn.disy920.slbot.selenium;

import cn.disy920.slbot.render.RichImageGenerator;
import cn.disy920.slbot.utils.system.OSChecker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;

import static cn.disy920.slbot.Main.*;
import static cn.disy920.slbot.utils.system.OSChecker.OSType.*;


public class FirefoxRichImageGenerator implements RichImageGenerator {

    private boolean hadInit = false;

    private final File firefoxBinaryFile = new File(PLUGIN_INSTANCE.getDataFolder(), "Selenium/Firefox/Binary/firefox");
    private final File firefoxDriverFile = new File(PLUGIN_INSTANCE.getDataFolder(), "Selenium/Firefox/Driver/geckodriver");
    private final File queryServerHtmlFile = new File(PLUGIN_INSTANCE.getDataFolder(), "Selenium/HTML/query-server.html");

    private FirefoxDriver driver;

    public FirefoxRichImageGenerator(){
        new File(PLUGIN_INSTANCE.getDataFolder(), "Selenium/Firefox").mkdirs();

        init();
    }


    @Override
    public void init(){
        if (!firefoxBinaryFile.exists() || firefoxBinaryFile.isDirectory()){
            RECORDER.error("未检测到Firefox浏览器二进制文件，请完整下载Firefox浏览器后，将其置于目录" + firefoxBinaryFile.getAbsolutePath() + "下");
            hadInit = false;
            return;
        }

        if (!firefoxDriverFile.exists() || firefoxDriverFile.isDirectory()){
            RECORDER.error("未检测到Firefox驱动，请完整下载适用于Selenium的Firefox驱动，将其置于目录" + firefoxDriverFile.getAbsolutePath() + "下");
            hadInit = false;
            return;
        }

        System.setProperty("webdriver.gecko.driver", firefoxDriverFile.getAbsolutePath());

        OSChecker.OSType type = OSChecker.checkOS();

        if (type == LINUX || type == MACOS){
            System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
        }
        else if (type == WINDOWS){
            System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "nul");
        }

        driver = new FirefoxDriver(
                new FirefoxOptions()
                        .addArguments("--headless")
                        .addArguments("--disable-extensions")
                        .addArguments("--no-sandbox")
                        .addArguments("--ignore-certificate-errors")
                        .addArguments("--allow-running-insecure-content")
                        .setBinary(firefoxBinaryFile.getAbsolutePath())
                        .setPageLoadTimeout(Duration.ofSeconds(3))
        );

        hadInit = true;
    }


    @Override
    public void reload(){
        destroy();
        hadInit = false;

        init();
    }


    @Override
    public void destroy(){
        if (driver != null){
            try {
                driver.quit();
            }
            catch (Exception ignored){}

            driver = null;
        }
    }


    /**
     * 通过HTML文件，渲染生成查服图片
     * @param currentNum 服务器当前人数
     * @param maxNum 服务器最大人数
     * @param imageHeight 生成图片的高度
     * @param text 仪表盘下方要显示的文字
     * @return 包含PNG格式图片的输入流，如果图片生成失败，则为null
     */
    @Override
    @Nullable
    public InputStream genQueryServerImage(int currentNum, int maxNum, int imageHeight, @NotNull String text, int theme){
        if (!hadInit){
            LOGGER.warn("Firefox尚未初始化完成，请检查一下或稍后再试");
            return null;
        }

        int height = imageHeight;
        if (height < 900){
            height = 900;
        }

        int maxGraduation = (currentNum / 50 + 1) * 50;  // 仪表盘上线
        int splitNum = maxGraduation / 10;  // 仪表盘精度

        if (!queryServerHtmlFile.exists()){
            return null;
        }

        try {
            driver.manage().window().setSize(new Dimension(1000, height));
            driver.get("file://" + queryServerHtmlFile.getAbsolutePath());

            // 执行JavaScript，将图片元素渲染进去
            driver.executeScript("changeTheme(%d)".formatted(theme));
            driver.executeScript("monitorOption.series[0].data[0].value=%d;monitorOption.series[1].data[0].value=%d;monitorOption.series[0].detail.formatter=\"{value} / %d\";monitorOption.series[0].max=%d;monitorOption.series[1].max=%d;monitorOption.series[0].splitNumber=%d;let textEle=document.createElement(\"p\");textEle.innerHTML=\"%s\";document.getElementsByClassName(\"container\")[0].appendChild(textEle);let monitorEle=document.getElementById(\"monitor\");let monitor=echarts.init(monitorEle);monitor.setOption(monitorOption);".formatted(currentNum, currentNum, maxNum, maxGraduation, maxGraduation, splitNum, text.replace("\n", "<br>").replace(" ", "&nbsp;")));

            // 渲染并截图
            return driver.getFullPageScreenshotAs(ImageOutputType.INPUT_STREAM);
        }
        catch (Exception e){
            LOGGER.error("图片生成过程发生错误，以下是错误的堆栈信息：");
            e.printStackTrace();
            return null;
        }
    }
}
