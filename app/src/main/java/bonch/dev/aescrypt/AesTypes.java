package bonch.dev.aescrypt;

public enum AesTypes {

    CTR("AES/CTR/NoPadding"),
    CBC("AES/CBC/PKCS7Padding");

    private String param;

    AesTypes(String param) {
        this.param=param;
    }

    public String getParam() {
        return param;
    }
}
