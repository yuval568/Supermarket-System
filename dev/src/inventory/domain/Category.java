package inventory.domain;

import java.util.ArrayList;
import java.util.List;

public class Category {

    private String name;
    private List<Category> subCategories;
    private Category parent;


    public Category(String name, Category parent) {

        if (name == null)
            throw new IllegalArgumentException("Name cannot be empty");
        this.name = name;
        this.parent = parent;
        this.subCategories = new ArrayList<>();
        if (parent != null){
            parent.addSubCategory(this);
        }
    }

    public Category(String name) {

        if (name == null)
            throw new IllegalArgumentException("Name cannot be empty");
        this.name = name;
        this.subCategories = new ArrayList<>();
    }


    public String getName() {
        return name;
    }

    public List<Category> getSubCategories() {
        return subCategories;
    }

    public String getFullName() {
        if (parent == null)
            return name;
        return parent.getFullName() + " -> " + name;
    }

    public Category getParent() {
        return parent;
    }

    public void addSubCategory(Category sub) {

        if (sub == null)
            return;
        for (Category existing : this.subCategories) {
            if (existing.getName().equals(sub.getName())) {
                return;
            }
        }
        this.subCategories.add(sub);
    }

    public boolean isSonOf(Category parent){

        if (this.getParent() == null)
            return false;

        if (this.getParent().getName().equals(parent.getName()))
            return true;

        return this.getParent().isSonOf(parent);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return name.equals(category.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}