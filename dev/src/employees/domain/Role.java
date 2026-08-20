package employees.domain;

import java.util.Objects;

public class Role {
    private String roleName;

    public Role(String roleName) {
        //We use .trim() to remove accidental spaces before or after the name
        this.roleName = roleName != null ? roleName.trim() : "";
    }

    public String getRoleName() { return roleName; }

    public void setRoleName(String roleName) { this.roleName = roleName != null ? roleName.trim() : ""; }

    @Override
    //Checks if two Role objects represent the exact same role
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;

        return roleName.equalsIgnoreCase(role.roleName);
    }

    @Override
    //Generates a numeric ID for the HashMap to find the object faster
    public int hashCode() {
        return Objects.hash(roleName.toLowerCase());
    }
}
