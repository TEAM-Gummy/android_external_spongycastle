package org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bouncycastle.util.io.Streams;

public class CMSProcessableInputStream implements CMSProcessable
{
    private InputStream input;
    private boolean used = false;

    public CMSProcessableInputStream(
        InputStream input)
    {
        this.input = input;
    }

    public InputStream read()
    {
        checkSingleUsage();

        return input;
    }

    public void write(OutputStream zOut)
        throws IOException, CMSException
    {
        checkSingleUsage();

        Streams.pipeAll(input, zOut);
        input.close();
    }

    public Object getContent()
    {
        return read();
    }

    private synchronized void checkSingleUsage()
    {
        if (used)
        {
            throw new IllegalStateException("CMSProcessableInputStream can only be used once");
        }

        used = true;
    }
}