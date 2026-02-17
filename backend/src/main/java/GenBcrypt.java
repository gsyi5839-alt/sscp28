import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenBcrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // Read password from args to avoid editing source file and stale compiled classes.
        String password = (args != null && args.length > 0) ? args[0] : "aa1010";
        String hash = encoder.encode(password);
        System.out.println(hash);
    }
}
