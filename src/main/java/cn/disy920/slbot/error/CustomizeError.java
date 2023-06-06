package cn.disy920.slbot.error;

@SuppressWarnings("ClassCanBeRecord")
public class CustomizeError implements Error {

    private final String info;
    private final int code;

    public CustomizeError(String info, int code){
        this.info = info;
        this.code = code;
    }

    @Override
    public String getInfo() {
        return this.info;
    }

    @Override
    public int getCode(){
        return this.code;
    }

    @Override
    public boolean equals(Object another){
        if (this == another) {
            return true;
        }
        return (another instanceof CustomizeError && this.code == ((CustomizeError) another).code && this.info.equals(((CustomizeError) another).info));
    }

    @Override
    public int hashCode(){
        return (int)(((long)this.info.hashCode() + this.code) & Integer.MAX_VALUE);
    }

    public CustomizeErrorBuilder createBuilder(){
        return new CustomizeErrorBuilder();
    }

    public CustomizeErrorBuilder createBuilder(String info){
        return new CustomizeErrorBuilder(info);
    }

    private static class CustomizeErrorBuilder{
        private final StringBuilder infoBuilder;

        CustomizeErrorBuilder(){
            this.infoBuilder = new StringBuilder();
        }

        CustomizeErrorBuilder(String initialInfo){
            this.infoBuilder = new StringBuilder(initialInfo);
        }

        public CustomizeErrorBuilder append(String info){
            infoBuilder.append(info);
            return this;
        }

        public CustomizeError build(int code){
            return new CustomizeError(infoBuilder.toString(), code);
        }

        @Override
        public boolean equals(Object another){
            if (this == another) {
                return true;
            }
            return (another instanceof CustomizeErrorBuilder && infoBuilder.compareTo(((CustomizeErrorBuilder) another).infoBuilder) == 0);
        }

        @Override
        public int hashCode(){
            return this.infoBuilder.toString().hashCode();
        }

    }
}
