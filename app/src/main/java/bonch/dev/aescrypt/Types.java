package bonch.dev.aescrypt;

public enum Types {

    CTR("AES/CTR/NoPadding"),
    CBC("AES/CBC/PKCS7Padding");

    private String param;

    Types(String param) {
        this.param=param;
    }

    public String getParam() {
        return param;
    }
}
