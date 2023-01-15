package io.liebrand.exoplayer2;




import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.StringTokenizer;

/**
 * A Ftp {@link DataSource}.
 */
public final class FtpDataSource extends BaseDataSource {

    /** {@link DataSource.Factory} for {@link DefaultHttpDataSource} instances. */
    public static final class Factory implements DataSource.Factory {


        @Nullable
        private TransferListener transferListener;

        /** Creates an instance. */
        public Factory() {
        }

        /**
         * Sets the {@link TransferListener} that will be used.
         *
         * <p>The default is {@code null}.
         *
         * <p>See {@link DataSource#addTransferListener(TransferListener)}.
         *
         * @param transferListener The listener that will be used.
         * @return This factory.
         */
        public FtpDataSource.Factory setTransferListener(@Nullable TransferListener transferListener) {
            this.transferListener = transferListener;
            return this;
        }


        @Override
        public DataSource createDataSource() {
            DataSource dataSource =
                    new FtpDataSource(this.transferListener);
            if (transferListener != null) {
                dataSource.addTransferListener(transferListener);
            }
            return dataSource;
        }
    }


    /**
     * Thrown when an error is encountered when trying to read from a {@link FtpDataSource}.
     */
    public static final class FtpDataSourceException extends IOException {

        public FtpDataSourceException(String message) {
            super(message);
        }

        public FtpDataSourceException(IOException cause) {
            super(cause);
        }

    }

    /**
     * The default maximum datagram packet size, in bytes.
     */
    public static final int DEFAULT_MAX_PACKET_SIZE = 8 *1024 * 1024;

    /**
     * The default socket timeout, in milliseconds.
     */
    public static final int DEFAULT_SOCKET_TIMEOUT_MILLIS = 8 * 1000;

    private final TransferListener listener;

    private DataSpec dataSpec;
    private boolean opened = false;
    private long bytesRemaining;

    private final FTPClient ftpClient;
    private InputStream stream;

    /**
     * @param listener An optional listener.
     */
    public FtpDataSource(TransferListener listener) {
        this(DEFAULT_SOCKET_TIMEOUT_MILLIS, DEFAULT_MAX_PACKET_SIZE, listener);
    }

    public FtpDataSource(int timeout, int bufferSize, TransferListener listener) {
        super(true);
        this.listener = listener;
        ftpClient = new FTPClient();
        ftpClient.setDataTimeout(timeout);
//	  ftpClient.setReceieveDataSocketBufferSize(bufferSize);
        Log.i("ftp", "new FtpDataSource created");
    }

    private long getFileSize(FTPClient ftp, String filePath) throws IOException {
        Log.i("ftp", "Retrieving file size");
        long fileSize = 0;
        FTPFile[] files = ftp.listFiles(filePath);
        if (files.length == 1 && files[0].isFile()) {
            fileSize = files[0].getSize();
        }
        return fileSize;
    }

    boolean isLogin = false, isConnect=false;
    long fileSize;
    @Override
    public long open(DataSpec dataSpec) throws FtpDataSourceException {
        this.dataSpec = dataSpec;
        String host = dataSpec.uri.getHost();
        int port = dataSpec.uri.getPort();
        if(port<=0) port= FTP.DEFAULT_PORT;
        String filePath = dataSpec.uri.getPath();
        String userInfo = dataSpec.uri.getUserInfo();
        Log.i("ftp", "ftp login:" + dataSpec.uri.toString() + " " + dataSpec.position);
        String user="anonymous",pass="";

        if(userInfo != null){
            StringTokenizer tok = new StringTokenizer(userInfo, ":@");
            if(tok.countTokens() > 0){
                user = tok.nextToken();
                if(tok.hasMoreTokens())
                    pass = tok.nextToken();
            }
        }

        try {
            long start_time = System.currentTimeMillis();
            Log.d("ftp", "Trying to connect: " + host +  " port " + port);
            Log.d("ftp", String.valueOf(InetAddress.getByName(host)));
            ftpClient.connect(host);
            isConnect = FTPReply.isPositiveCompletion(ftpClient.getReplyCode());
            if(!isConnect)
                throw	new FtpDataSourceException("connect failed.");
            Log.i("ftp", "ftp connect use:" + (System.currentTimeMillis() - start_time) );

            isLogin = ftpClient.login(user, pass);
            Log.i("ftp", "ftp login use:" + (System.currentTimeMillis() - start_time) );
            Log.i("ftp", "ftp login:" + user + ":" + pass + "@" + host + ":" + port + " - " + isLogin);
            if(!isLogin)
                throw new FtpDataSourceException("login failed.");

            fileSize = getFileSize(ftpClient, filePath);

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.COMPRESSED_TRANSFER_MODE);
            ftpClient.setRestartOffset(dataSpec.position);
            stream = ftpClient.retrieveFileStream(filePath);
            int LENGTH_UNBOUNDED = -1;
            bytesRemaining = dataSpec.length == LENGTH_UNBOUNDED ? fileSize - dataSpec.position
                    : dataSpec.length;
            Log.i("ftp", "Bytes remaining: " + bytesRemaining);
        } catch (IOException e) {
            throw new FtpDataSourceException(e);
        }

        opened = true;
        if (listener != null) {
            //ML ADD
            listener.onTransferStart(this, dataSpec, true);
        }
        return bytesRemaining;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws FtpDataSourceException {
        if (bytesRemaining == 0) {
            return -1;
        } else {
            int bytesRead = 0;
            try {
                bytesRead = stream.read(buffer, offset, (int) Math.min(bytesRemaining, readLength));
            } catch (IOException e) {
                throw new FtpDataSourceException(e);
            }

            if (bytesRead > 0) {
                bytesRemaining -= bytesRead;
                if (listener != null) {
                    listener.onBytesTransferred(this, dataSpec,true, bytesRead);
                }
            }
            return bytesRead;
        }
    }

    @Override
    public void close() {
        Log.i("ftp", "bye");
        if (opened) {
            opened = false;
            try {
                stream.close();
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                if (listener != null) {
                    listener.onTransferEnd(this, dataSpec, true);
                }
            }
        }
    }

    @Override
    public Uri getUri() {
        return dataSpec == null ? null : dataSpec.uri;
    }

}