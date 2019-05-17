package othellocrown.account.common.utils;

class JwtPayload {
    private String username;
    private String email;

    JwtPayload(String username, String email) {
        this.username = username;
        this.email = email;
    }
}
