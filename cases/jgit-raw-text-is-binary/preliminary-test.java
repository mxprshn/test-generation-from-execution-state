package org.eclipse.jgit.diff;

import org.junit.jupiter.api.Test;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.ByteBuffer;
import org.eclipse.jgit.diff.RawText;
import java.lang.*;

class RawTextReproducedTests {

    @Test
    void isBinaryReproducedTest() throws IOException {
        ByteBuffer v = ByteBuffer.wrap(Files.readAllBytes(new File(RawTextReproducedTests.class.getResource("/rawTextReproducedTests/byte_2.array").getFile()).toPath()));
        byte[] v1 = new byte[v.getInt()];
        v.get(v1);
        RawText.isBinary(v1, 1081, true);
    }

    @Test
    void isBinaryReproducedTest1() throws IOException {
        ByteBuffer v = ByteBuffer.wrap(Files.readAllBytes(new File(RawTextReproducedTests.class.getResource("/rawTextReproducedTests/byte_3.array").getFile()).toPath()));
        byte[] v1 = new byte[v.getInt()];
        v.get(v1);
        RawText.isBinary(v1, 1082, true);
    }
}
