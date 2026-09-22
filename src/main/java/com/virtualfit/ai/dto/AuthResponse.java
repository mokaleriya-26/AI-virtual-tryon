package com.virtualfit.ai.dto;

public class AuthResponse {
    private String token;
    private UserDto user;

    public AuthResponse() {}

    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }

    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public static class AuthResponseBuilder {
        private String token;
        private UserDto user;

        public AuthResponseBuilder token(String token) { this.token = token; return this; }
        public AuthResponseBuilder user(UserDto user) { this.user = user; return this; }
        public AuthResponse build() { return new AuthResponse(token, user); }
    }

    public static class UserDto {
        private String id;
        private String name;
        private String email;

        public UserDto() {}

        public UserDto(String id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public static UserDtoBuilder builder() {
            return new UserDtoBuilder();
        }

        public static class UserDtoBuilder {
            private String id;
            private String name;
            private String email;

            public UserDtoBuilder id(String id) { this.id = id; return this; }
            public UserDtoBuilder name(String name) { this.name = name; return this; }
            public UserDtoBuilder email(String email) { this.email = email; return this; }
            public UserDto build() { return new UserDto(id, name, email); }
        }
    }
}
