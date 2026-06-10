import java.nio.file.Paths;

public class Test {
    public static void main(String[] args) {
        System.out.println(Paths.get("uploads").toUri().toString());
        System.out.println(Paths.get("uploads").toAbsolutePath().toUri().toString());
    }
}
