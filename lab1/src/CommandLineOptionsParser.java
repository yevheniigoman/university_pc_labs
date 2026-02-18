import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CommandLineOptionsParser {
    public static CommandLineOptions parse(String[] args) {
        int dim = 5;
        int threads = 1;
        boolean print = false;

        List<String> argsList = Arrays.asList(args);
        Iterator<String> iterator = argsList.iterator();
        while (iterator.hasNext()) {
            String value = iterator.next();
            switch (value) {
                case "--dim" -> dim = Integer.parseInt(iterator.next());
                case "--threads" -> threads = Integer.parseInt(iterator.next());
                case "--print" -> print = true;
            }
        }
        return new CommandLineOptions(dim, threads, print);
    }
}
