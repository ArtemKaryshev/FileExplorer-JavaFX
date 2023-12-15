package artem.maven;

import de.saxsys.javafx.test.JfxRunner;
import javafx.scene.control.Label;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Test class for Functions class methods.
 */
@RunWith(JfxRunner.class)
public class FunctionsTest {

    private Functions functions;
    private Label title;

    @Before
    public void setUp() {
        // Initialize Functions instance and title Label for testing
        functions = new Functions();
        title = new Label();
    }

    /**
     * Test to check the owner retrieval for a file path.
     */
    @Test
    public void testOwner1() {
        String path = "C:\\Windows\\notepad.exe";
        String fileOwner = "TrustedInstaller";

        String func_owner = functions.owner(path);
        assertEquals(fileOwner, func_owner);
    }

    /**
     * Test to check the owner retrieval for a file path (expected result: "-").
     */
    @Test
    public void testOwner2() {
        String path1 = "C:\\DumpStack.log";
        String fileowner1 = "-";

        String func_owner1 = functions.owner(path1);
        assertEquals(fileowner1, func_owner1);
    }

    /**
     * Test the goBack method for a specific directory path.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testGoBack1 () throws IOException {
        functions.currentDirectoryPath = "C:\\Users\\artem\\OneDrive\\Desktop\\Приложения\\AAA Logo\\App";
        functions.goBack(title);
        String predictedBack = "C:\\Users\\artem\\OneDrive\\Desktop\\Приложения\\AAA Logo";

        assertEquals(predictedBack, functions.currentDirectoryPath);
    }

    /**
     * Test the goBack method for the root directory.
     *
     * @throws IOException if an I/O error occurs
     */
    @Test
    public void testGoBack2 () throws IOException {
        functions.currentDirectoryPath = "C:\\";
        functions.goBack(title);
        String predictedBack = "C:\\";

        assertEquals(predictedBack, functions.currentDirectoryPath);
    }

    /**
     * Test to calculate the size of a specified folder path.
     */
    @Test
    public void calcSizeTest() {
        String path = "D:\\Downloads\\Telegram Desktop";
        double predictSize = 405292610; // Predicted size in bytes
        double programSize = functions.calculateFolderSize(path);
        assertEquals(predictSize, programSize, 0.001);
    }
}
