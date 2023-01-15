package io.liebrand.exoplayer2;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.io.IOException;

public class ExDataSource implements DataSource {

    /** {@link DataSource.Factory} for {@link DefaultDataSource} instances. */
    public static final class Factory implements DataSource.Factory {

        private final Context context;
        private final DataSource.Factory baseDataSourceFactory;
        @Nullable private TransferListener transferListener;

        /**
         * Creates an instance.
         *
         * @param context A context.
         */
        public Factory(Context context) {
            this(context, new FtpDataSource.Factory());
        }

        /**
         * Creates an instance.
         *
         * @param context A context.
         * @param baseDataSourceFactory The {@link DataSource.Factory} to be used to create base {@link
         *     DataSource DataSources} for {@link DefaultDataSource} instances. The base {@link
         *     DataSource} is  an {@link FtpDataSource}, and is responsible for fetching data
         *     over FTP.
         */
        public Factory(Context context, DataSource.Factory baseDataSourceFactory) {
            this.context = context.getApplicationContext();
            this.baseDataSourceFactory = baseDataSourceFactory;
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
        public DataSource.Factory setTransferListener(@Nullable TransferListener transferListener) {
            this.transferListener = transferListener;
            return this;
        }

        @Override
        public DefaultDataSource createDataSource() {
            DefaultDataSource dataSource =
                    new DefaultDataSource(context, baseDataSourceFactory.createDataSource());
            if (transferListener != null) {
                dataSource.addTransferListener(transferListener);
            }
            return dataSource;
        }
    }

    private static final String TAG = "ExDataSource";



    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        return 0;
    }

    @Override
    public void addTransferListener(TransferListener transferListener) {

    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        return 0;
    }

    @Nullable
    @Override
    public Uri getUri() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}
