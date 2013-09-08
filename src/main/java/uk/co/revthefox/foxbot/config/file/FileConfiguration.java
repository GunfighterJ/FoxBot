package uk.co.revthefox.foxbot.config.file;

import com.google.common.base.Preconditions;
import uk.co.revthefox.foxbot.FoxBot;
import uk.co.revthefox.foxbot.config.Configuration;
import uk.co.revthefox.foxbot.config.InvalidConfigurationException;
import uk.co.revthefox.foxbot.config.MemoryConfiguration;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * This is a base class for all File based implementations of
 * {@link Configuration}
 */
public abstract class FileConfiguration extends MemoryConfiguration
{

    /**
     * Creates an empty {@link FileConfiguration} with no default values.
     */
    public FileConfiguration()
    {
        super();
    }

    /**
     * Creates an empty {@link FileConfiguration} using the specified
     * {@link Configuration} as a source for all default values.
     *
     * @param defaults Default value provider
     */
    public FileConfiguration(Configuration defaults)
    {
        super(defaults);
    }

    /**
     * Saves this {@link FileConfiguration} to the specified location.
     * <p />
     * If the file does not exist, it will be created. If already exists, it
     * will be overwritten. If it cannot be overwritten or created, an exception
     * will be thrown.
     *
     * @param file File to save to.
     * @throws IOException Thrown when the given file cannot be written to for
     * any reason.
     * @throws IllegalArgumentException Thrown when file is null.
     */
    public void save(File file) throws IOException
    {
        Preconditions.checkNotNull(file, "File cannot be null");

        File parent = file.getCanonicalFile().getParentFile();
        if (parent == null)
        {
            /*
             * The given directory is a filesystem root. All zero of its ancestors
             * exist. This doesn't mean that the root itself exists -- consider x:\ on
             * a Windows machine without such a drive -- or even that the caller can
             * create it, but this method makes no such guarantees even for non-root
             * files.
             */
            return;
        }
        parent.mkdirs();
        if (!parent.isDirectory())
        {
            throw new IOException("Unable to create parent directories of " + file);
        }

        String data = saveToString();

        FileWriter writer = new FileWriter(file);

        try
        {
            writer.write(data);
        }
        finally
        {
            writer.close();
        }
    }

    /**
     * Saves this {@link FileConfiguration} to the specified location.
     * <p />
     * If the file does not exist, it will be created. If already exists, it
     * will be overwritten. If it cannot be overwritten or created, an exception
     * will be thrown.
     *
     * @param file File to save to.
     * @throws IOException Thrown when the given file cannot be written to for
     * any reason.
     * @throws IllegalArgumentException Thrown when file is null.
     */
    public void save(String file) throws IOException
    {
        Preconditions.checkNotNull(file, "File cannot be null");

        save(new File(file));
    }

    /**
     * Saves this {@link FileConfiguration} to a string, and returns it.
     *
     * @return String containing this configuration.
     */
    public abstract String saveToString();

    /**
     * Loads this {@link FileConfiguration} from the specified location.
     * <p />
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given file.
     * <p />
     * If the file cannot be loaded for any reason, an exception will be thrown.
     *
     * @param file File to load from.
     * @throws FileNotFoundException Thrown when the given file cannot be
     * opened.
     * @throws IOException Thrown when the given file cannot be read.
     * @throws InvalidConfigurationException Thrown when the given file is not a
     * valid Configuration.
     * @throws IllegalArgumentException Thrown when file is null.
     */
    public void load(File file) throws FileNotFoundException, IOException, InvalidConfigurationException
    {
        Preconditions.checkNotNull(file, "File cannot be null");

        load(new FileInputStream(file));
    }

    /**
     * Loads this {@link FileConfiguration} from the specified stream.
     * <p />
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given stream.
     *
     * @param stream Stream to load from
     * @throws IOException Thrown when the given file cannot be read.
     * @throws InvalidConfigurationException Thrown when the given file is not a
     * valid Configuration.
     * @throws IllegalArgumentException Thrown when stream is null.
     */
    public void load(InputStream stream) throws IOException, InvalidConfigurationException
    {
        Preconditions.checkNotNull(stream, "Stream cannot be null");

        InputStreamReader reader = new InputStreamReader(stream);
        StringBuilder builder = new StringBuilder();
        BufferedReader input = new BufferedReader(reader);


        try
        {
            String line;

            while ((line = input.readLine()) != null)
            {
                builder.append(line);
                builder.append('\n');
            }
        }
        finally
        {
            input.close();
        }

        loadFromString(builder.toString());
    }

    /**
     * Loads this {@link FileConfiguration} from the specified location.
     * <p />
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given file.
     * <p />
     * If the file cannot be loaded for any reason, an exception will be thrown.
     *
     * @param file File to load from.
     * @throws FileNotFoundException Thrown when the given file cannot be
     * opened.
     * @throws IOException Thrown when the given file cannot be read.
     * @throws InvalidConfigurationException Thrown when the given file is not a
     * valid Configuration.
     * @throws IllegalArgumentException Thrown when file is null.
     */
    public void load(String file) throws FileNotFoundException, IOException, InvalidConfigurationException
    {
        Preconditions.checkNotNull(file, "File cannot be null");

        load(new File(file));
    }

    /**
     * Loads this {@link FileConfiguration} from the specified string, as
     * opposed to from file.
     * <p />
     * All the values contained within this configuration will be removed,
     * leaving only settings and defaults, and the new values will be loaded
     * from the given string.
     * <p />
     * If the string is invalid in any way, an exception will be thrown.
     *
     * @param contents Contents of a Configuration to load.
     * @throws InvalidConfigurationException Thrown if the specified string is
     * invalid.
     * @throws IllegalArgumentException Thrown if contents is null.
     */
    public abstract void loadFromString(String contents) throws InvalidConfigurationException;

    /**
     * Compiles the header for this {@link FileConfiguration} and returns the
     * result.
     * <p />
     * This will use the header from
     * {@link #options()} -> {@link FileConfigurationOptions#header()},
     * respecting the rules of {@link FileConfigurationOptions#copyHeader()} if
     * set.
     *
     * @return Compiled header
     */
    protected abstract String buildHeader();

    @Override
    public FileConfigurationOptions options()
    {
        if (options == null)
        {
            options = new FileConfigurationOptions(this);
        }

        return (FileConfigurationOptions) options;
    }

    public void saveResource(File dataFolder, String resourcePath, boolean replace)
    {
        if (resourcePath == null || resourcePath.equals(""))
        {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);

        if (in == null)
        {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in.");
        }

        File outFile = new File(dataFolder, resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(dataFolder, resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

        if (!outDir.exists())
        {
            outDir.mkdirs();
        }

        try
        {
            if (!outFile.exists() || replace)
            {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public InputStream getResource(String filename)
    {
        if (filename == null)
        {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try
        {
            URL url = getClassLoader().getResource(filename);

            if (url == null)
            {
                return null;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public ClassLoader getClassLoader()
    {
        return FoxBot.class.getClassLoader();
    }
}
