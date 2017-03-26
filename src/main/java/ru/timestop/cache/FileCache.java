package ru.timestop.cache;

import ru.timestop.cache.exception.BucketException;

import java.io.*;
import java.util.UUID;

/**
 * @author t.i.m.e.s.t.o.p
 * @version 1.0.0
 * @since 16.08.2018
 */
public class FileCache<K extends Serializable, V extends Serializable> implements Cache<K, V> {

    private final String rootDirectory;
    private int size;

    public FileCache(String rootDir) {
        this.rootDirectory = rootDir + File.separator + getUUID();
        this.size = 0;
    }

    @Override
    public synchronized void put(K key, V value) {
        File rootDir = new File(rootDirectory);
        rootDir.mkdirs();
        File objFile = getObjectFile(rootDirectory, key.hashCode());
        File tempFile = getTempFile(rootDirectory);
        if (objFile.exists()) {
            V result = null;
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            ObjectInputStream ois = null;

            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            ObjectOutputStream oos = null;
            try {
                fis = new FileInputStream(objFile);
                bis = new BufferedInputStream(fis);
                ois = new ObjectInputStream(bis);
                fos = new FileOutputStream(tempFile);
                bos = new BufferedOutputStream(fos);
                oos = new ObjectOutputStream(bos);
                result = cleanFile(key, ois, oos);
                oos.writeObject(key);
                oos.writeObject(value);
                oos.writeObject(new EndOfFile());
                oos.flush();
            } catch (IOException | ClassNotFoundException e) {
                throw new BucketException(e);
            } finally {
                closeQuietly(ois);
                closeQuietly(bis);
                closeQuietly(fis);
                closeQuietly(oos);
                closeQuietly(bos);
                closeQuietly(fos);
            }
            if (result == null) {
                objFile.delete();
                tempFile.renameTo(objFile);
                size++;
            } else {
                tempFile.delete();
            }
        } else {
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            ObjectOutputStream oos = null;
            try {
                fos = new FileOutputStream(objFile);
                bos = new BufferedOutputStream(fos);
                oos = new ObjectOutputStream(bos);
                oos.writeObject(key);
                oos.writeObject(value);
                oos.writeObject(new EndOfFile());
                oos.flush();
            } catch (IOException e) {
                throw new BucketException(e);
            } finally {
                closeQuietly(oos);
                closeQuietly(bos);
                closeQuietly(fos);
            }
            size++;
        }
    }

    @Override
    public synchronized V get(K key) {
        File objFile = getObjectFile(rootDirectory, key.hashCode());
        if (objFile.exists()) {
            FileInputStream inputStream = null;
            BufferedInputStream is = null;
            ObjectInputStream ois = null;
            try {
                inputStream = new FileInputStream(objFile);
                is = new BufferedInputStream(inputStream);
                ois = new ObjectInputStream(is);
                int maxSize = size;
                int inc = 0;
                while (maxSize >= inc++) {
                    Object obj = ois.readObject();
                    if (obj instanceof EndOfFile) {
                        break;
                    }
                    K lkey = (K) obj;
                    V value = (V) ois.readObject();
                    if (lkey.equals(key)) {
                        return value;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new BucketException(e);
            } finally {
                closeQuietly(ois);
                closeQuietly(is);
                closeQuietly(inputStream);
            }
        }
        return null;
    }

    @Override
    public synchronized V remove(K key) {
        V result = null;
        File objFile = getObjectFile(rootDirectory, key.hashCode());
        if (objFile.exists()) {
            File tempFile = getTempFile(rootDirectory);
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            ObjectInputStream ois = null;

            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            ObjectOutputStream oos = null;
            try {
                fis = new FileInputStream(objFile);
                bis = new BufferedInputStream(fis);
                ois = new ObjectInputStream(bis);
                fos = new FileOutputStream(tempFile);
                bos = new BufferedOutputStream(fos);
                oos = new ObjectOutputStream(bos);
                result = cleanFile(key, ois, oos);
                oos.writeObject(new EndOfFile());
                oos.flush();
            } catch (IOException | ClassNotFoundException e) {
                throw new BucketException(e);
            } finally {
                closeQuietly(ois);
                closeQuietly(bis);
                closeQuietly(fis);
                closeQuietly(oos);
                closeQuietly(bos);
                closeQuietly(fos);
            }
            if (result != null) {
                objFile.delete();
                tempFile.renameTo(objFile);
                size--;
            } else {
                tempFile.delete();
            }
        }
        return result;
    }

    @Override
    public synchronized void clear() {
        File d = new File(rootDirectory);
        if (!deleteDirectory(d) && d.exists()) {
            throw new BucketException("Bucket not clear");
        }
        size = 0;
    }

    @Override
    public synchronized int getSize() {
        return size;
    }


    /**
     * @param key of removed object
     * @param in  stream of objects list
     * @param out stream of cleaned objects list
     * @return finded object or null if object not found
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private V cleanFile(K key, ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException {
        V result = null;
        int maxSize = size;
        int inc = 0;
        while (maxSize >= inc++) {
            Object obj = in.readObject();
            if (obj instanceof EndOfFile) {
                break;
            }
            K lkey = (K) obj;
            V value = (V) in.readObject();
            if (lkey.equals(key)) {
                result = value;
            } else {
                out.writeObject(lkey);
                out.writeObject(value);
            }
        }
        return result;
    }

    /**
     * @param directory for delete
     * @return true if directory deleted
     */
    private static boolean deleteDirectory(File directory) {
        if (directory.exists()) {
            for (File subfile : directory.listFiles()) {
                if (subfile.isDirectory()) {
                    deleteDirectory(subfile);
                } else {
                    subfile.delete();
                }
            }
        }
        return directory.delete();
    }

    /**
     * @param bucketDir with object files
     * @param hashCode  of key
     * @return objects file corresponds with hashCode
     */
    private static File getObjectFile(String bucketDir, int hashCode) {
        String fileName = "hc" + String.valueOf(hashCode) + ".obj";
        return new File(bucketDir + File.separator + fileName);
    }

    /**
     * @param bucketDir with object files
     * @return temp objects file corresponds with hashCode
     */
    private static File getTempFile(String bucketDir) {
        String fileName = getUUID() + ".temp";
        return new File(bucketDir + File.separator + fileName);
    }

    /**
     * @return new random UUID
     */
    private static String getUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * close quietly stream
     *
     * @param closeable stream for close
     */
    private static void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
            //SKIP
            e.printStackTrace();
        }
    }

    /**
     * class for mark end of file
     */
    private static class EndOfFile implements Serializable {

    }
}
