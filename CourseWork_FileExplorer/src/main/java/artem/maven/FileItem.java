package artem.maven;

public class FileItem {
    private final String name;
    private final String type;
    private final String size_res;
    private final String owner;
    private final String date;


    public FileItem(String name, String type, String size_res, String owner, String date) {
        this.name = name;
        this.type = type;
        this.size_res = size_res;
        this.owner = owner;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSize_res() {
        return size_res;
    }

    public String getOwner() {
        return owner;
    }

    public String getDate() {
        return date;
    }
}
