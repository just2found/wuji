package libs.source.common.utils;

import android.os.Build;
import android.os.StatFs;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class IOUtils {

    public static void closeQuietly(@Nullable Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void flushQuietly(@Nullable Flushable flushable) {
        if (flushable == null) return;
        try {
            flushable.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static InputStream toInputStream(CharSequence input) {
        return new ByteArrayInputStream(input.toString().getBytes());
    }

    public static InputStream toInputStream(CharSequence input, @NonNull String encoding) throws UnsupportedEncodingException {
        byte[] bytes = input.toString().getBytes(encoding);
        return new ByteArrayInputStream(bytes);
    }

    public static BufferedInputStream toBufferedInputStream(InputStream inputStream) {
        return inputStream instanceof BufferedInputStream ? (BufferedInputStream) inputStream : new BufferedInputStream(inputStream);
    }

    public static BufferedOutputStream toBufferedOutputStream(OutputStream outputStream) {
        return outputStream instanceof BufferedOutputStream ? (BufferedOutputStream) outputStream : new BufferedOutputStream(outputStream);
    }

    public static BufferedReader toBufferedReader(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    public static BufferedWriter toBufferedWriter(Writer writer) {
        return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
    }

    public static String toString(@NonNull InputStream input) throws IOException {
        return new String(toByteArray(input));
    }

    public static String toString(@NonNull InputStream input, @NonNull String encoding) throws IOException {
        return new String(toByteArray(input), encoding);
    }

    public static String toString(@NonNull Reader input) throws IOException {
        return new String(toByteArray(input));
    }

    public static String toString(@NonNull Reader input, @NonNull String encoding) throws IOException {
        return new String(toByteArray(input), encoding);
    }

    public static String toString(@NonNull byte[] byteArray) {
        return new String(byteArray);
    }

    public static String toString(@NonNull byte[] byteArray, @NonNull String encoding) {
        try {
            return new String(byteArray, encoding);
        } catch (UnsupportedEncodingException e) {
            return new String(byteArray);
        }
    }

    public static byte[] toByteArray(Object input) {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(input);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(oos);
            IOUtils.closeQuietly(baos);
        }
        return null;
    }

    @Nullable
    public static Object toObject(@Nullable byte[] input) {
        if (input == null) return null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            bais = new ByteArrayInputStream(input);
            ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(ois);
            IOUtils.closeQuietly(bais);
        }
        return null;
    }

    public static byte[] toByteArray(@Nullable CharSequence input) {
        if (input == null) return new byte[0];
        return input.toString().getBytes();
    }

    public static byte[] toByteArray(@Nullable CharSequence input, @NonNull String encoding) throws UnsupportedEncodingException {
        if (input == null) return new byte[0];
        else return input.toString().getBytes(encoding);
    }

    public static byte[] toByteArray(@NonNull InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(input, output);
        output.close();
        return output.toByteArray();
    }

    public static byte[] toByteArray(@NonNull Reader input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(input, output);
        output.close();
        return output.toByteArray();
    }

    public static byte[] toByteArray(@NonNull Reader input, @NonNull String encoding) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        write(input, output, encoding);
        output.close();
        return output.toByteArray();
    }

    public static char[] toCharArray(CharSequence input) throws IOException {
        CharArrayWriter output = new CharArrayWriter();
        write(input, output);
        return output.toCharArray();
    }

    public static char[] toCharArray(@NonNull InputStream input) throws IOException {
        CharArrayWriter output = new CharArrayWriter();
        write(input, output);
        return output.toCharArray();
    }

    public static char[] toCharArray(@NonNull InputStream input, @NonNull String encoding) throws IOException {
        CharArrayWriter output = new CharArrayWriter();
        write(input, output, encoding);
        return output.toCharArray();
    }

    public static char[] toCharArray(@NonNull Reader input) throws IOException {
        CharArrayWriter output = new CharArrayWriter();
        write(input, output);
        return output.toCharArray();
    }

    @NonNull
    public static List<String> readLines(@NonNull InputStream input, @NonNull String encoding) throws IOException {
        Reader reader = new InputStreamReader(input, encoding);
        return readLines(reader);
    }

    @NonNull
    public static List<String> readLines(@NonNull InputStream input) throws IOException {
        Reader reader = new InputStreamReader(input);
        return readLines(reader);
    }

    @NonNull
    public static List<String> readLines(Reader input) throws IOException {
        BufferedReader reader = toBufferedReader(input);
        List<String> list = new ArrayList<String>();
        String line = reader.readLine();
        while (line != null) {
            list.add(line);
            line = reader.readLine();
        }
        return list;
    }

    public static void write(@Nullable byte[] data, @NonNull OutputStream output) throws IOException {
        if (data != null) output.write(data);
    }

    public static void write(@Nullable byte[] data, @NonNull Writer output) throws IOException {
        if (data != null) output.write(new String(data));
    }

    public static void write(@Nullable byte[] data, @NonNull Writer output, @NonNull String encoding) throws IOException {
        if (data != null) output.write(new String(data, encoding));
    }

    public static void write(@Nullable char[] data, @NonNull Writer output) throws IOException {
        if (data != null) output.write(data);
    }

    public static void write(@Nullable char[] data, @NonNull OutputStream output) throws IOException {
        if (data != null) output.write(new String(data).getBytes());
    }

    public static void write(@Nullable char[] data, @NonNull OutputStream output, @NonNull String encoding) throws IOException {
        if (data != null) output.write(new String(data).getBytes(encoding));
    }

    public static void write(@Nullable CharSequence data, @NonNull Writer output) throws IOException {
        if (data != null) output.write(data.toString());
    }

    public static void write(@Nullable CharSequence data, @NonNull OutputStream output) throws IOException {
        if (data != null) output.write(data.toString().getBytes());
    }

    public static void write(@Nullable CharSequence data, @NonNull OutputStream output, @NonNull String encoding) throws IOException {
        if (data != null) output.write(data.toString().getBytes(encoding));
    }

    public static void write(InputStream inputStream, @NonNull OutputStream outputStream) throws IOException {
        int len;
        byte[] buffer = new byte[4096];
        while ((len = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, len);
    }

    public static void write(@NonNull Reader input, @NonNull OutputStream output) throws IOException {
        Writer out = new OutputStreamWriter(output);
        write(input, out);
        out.flush();
    }

    public static void write(@NonNull InputStream input, @NonNull Writer output) throws IOException {
        Reader in = new InputStreamReader(input);
        write(in, output);
    }

    public static void write(@NonNull Reader input, @NonNull OutputStream output, @NonNull String encoding) throws IOException {
        Writer out = new OutputStreamWriter(output, encoding);
        write(input, out);
        out.flush();
    }

    public static void write(@NonNull InputStream input, @NonNull OutputStream output, @NonNull String encoding) throws IOException {
        Reader in = new InputStreamReader(input, encoding);
        write(in, output);
    }

    public static void write(@NonNull InputStream input, @NonNull Writer output, @NonNull String encoding) throws IOException {
        Reader in = new InputStreamReader(input, encoding);
        write(in, output);
    }

    public static void write(Reader input, @NonNull Writer output) throws IOException {
        int len;
        char[] buffer = new char[4096];
        while (-1 != (len = input.read(buffer))) output.write(buffer, 0, len);
    }

    public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException {
        input1 = toBufferedInputStream(input1);
        input2 = toBufferedInputStream(input2);

        int ch = input1.read();
        while (-1 != ch) {
            int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
            ch = input1.read();
        }

        int ch2 = input2.read();
        return ch2 == -1;
    }

    public static boolean contentEquals(Reader input1, Reader input2) throws IOException {
        input1 = toBufferedReader(input1);
        input2 = toBufferedReader(input2);

        int ch = input1.read();
        while (-1 != ch) {
            int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
            ch = input1.read();
        }

        int ch2 = input2.read();
        return ch2 == -1;
    }

    public static boolean contentEqualsIgnoreEOL(Reader input1, Reader input2) throws IOException {
        BufferedReader br1 = toBufferedReader(input1);
        BufferedReader br2 = toBufferedReader(input2);

        String line1 = br1.readLine();
        String line2 = br2.readLine();
        while ((line1 != null) && (line2 != null) && (line1.equals(line2))) {
            line1 = br1.readLine();
            line2 = br2.readLine();
        }
        return line1 != null && (line2 == null || line1.equals(line2));
    }

    /**
     * Access to a directory available size.
     *
     * @param path path.
     * @return space size.
     */
    public static long getDirSize(String path) {
        StatFs stat;
        try {
            stat = new StatFs(path);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
        if (Build.VERSION.SDK_INT >= 18)
            return getStatFsSize(stat, "getBlockSizeLong", "getAvailableBlocksLong");
        else return getStatFsSize(stat, "getBlockSize", "getAvailableBlocks");
    }

    private static long getStatFsSize(@NonNull StatFs statFs, String blockSizeMethod, String availableBlocksMethod) {
        try {
            Method getBlockSizeMethod = statFs.getClass().getMethod(blockSizeMethod);
            getBlockSizeMethod.setAccessible(true);

            Method getAvailableBlocksMethod = statFs.getClass().getMethod(availableBlocksMethod);
            getAvailableBlocksMethod.setAccessible(true);

            long blockSize = (Long) getBlockSizeMethod.invoke(statFs);
            long availableBlocks = (Long) getAvailableBlocksMethod.invoke(statFs);
            return blockSize * availableBlocks;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * If the folder can be written.
     *
     * @param path part_url.
     * @return True: success, or false: failure.
     */
    public static boolean canWrite(@NonNull String path) {
        return new File(path).canWrite();
    }

    /**
     * If the folder can be read.
     *
     * @param path part_url.
     * @return True: success, or false: failure.
     */
    public static boolean canRead(@NonNull String path) {
        return new File(path).canRead();
    }

    /**
     * Create a folder, If the folder exists is not created.
     *
     * @param folderPath folder path.
     * @return True: success, or false: failure.
     */
    public static boolean createFolder(@NonNull String folderPath) {
        if (!TextUtils.isEmpty(folderPath)) {
            File folder = new File(folderPath);
            return createFolder(folder);
        }
        return false;
    }

    /**
     * Create a folder, If the folder exists is not created.
     *
     * @param targetFolder folder part_url.
     * @return True: success, or false: failure.
     */
    public static boolean createFolder(File targetFolder) {
        if (targetFolder.exists()) {
            if (targetFolder.isDirectory()) return true;
            //noinspection ResultOfMethodCallIgnored
            targetFolder.delete();
        }
        return targetFolder.mkdirs();
    }

    /**
     * Create a folder, If the folder exists is not created.
     *
     * @param folderPath folder path.
     * @return True: success, or false: failure.
     */
    public static boolean createNewFolder(@NonNull String folderPath) {
        return delFileOrFolder(folderPath) && createFolder(folderPath);
    }

    /**
     * Create a folder, If the folder exists is not created.
     *
     * @param targetFolder folder path.
     * @return True: success, or false: failure.
     */
    public static boolean createNewFolder(@NonNull File targetFolder) {
        return delFileOrFolder(targetFolder) && createFolder(targetFolder);
    }

    /**
     * Create a file, If the file exists is not created.
     *
     * @param filePath file path.
     * @return True: success, or false: failure.
     */
    public static boolean createFile(@NonNull String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            return createFile(file);
        }
        return false;
    }

    /**
     * Create a file, If the file exists is not created.
     *
     * @param targetFile file.
     * @return True: success, or false: failure.
     */
    public static boolean createFile(File targetFile) {
        if (targetFile.exists()) {
            if (targetFile.isFile()) return true;
            delFileOrFolder(targetFile);
        }
        try {
            return targetFile.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Create a new file, if the file exists, delete and create again.
     *
     * @param filePath file path.
     * @return True: success, or false: failure.
     */
    public static boolean createNewFile(@NonNull String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);
            return createNewFile(file);
        }
        return false;
    }

    /**
     * Create a new file, if the file exists, delete and create again.
     *
     * @param targetFile file.
     * @return True: success, or false: failure.
     */
    public static boolean createNewFile(File targetFile) {
        if (targetFile.exists()) delFileOrFolder(targetFile);
        try {
            return targetFile.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Delete file or folder.
     *
     * @param path path.
     * @return is succeed.
     * @see #delFileOrFolder(File)
     */
    public static boolean delFileOrFolder(@NonNull String path) {
        if (TextUtils.isEmpty(path)) return false;
        return delFileOrFolder(new File(path));
    }

    /**
     * Delete file or folder.
     *
     * @param file file.
     * @return is succeed.
     * @see #delFileOrFolder(String)
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean delFileOrFolder(@Nullable File file) {
        if (file == null || !file.exists()) {
            // do nothing
        } else if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File sonFile : files) {
                    delFileOrFolder(sonFile);
                }
            }
            file.delete();
        }
        return true;
    }

    public static void close(@Nullable Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}