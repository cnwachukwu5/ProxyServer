
import java.io.*;

public class HttpResponse {

    final static String CRLF = "\r\n";

    final static int BUF_SIZE = 819200;//1819200;
    final static int MAX_OBJECT_SIZE = 400000;

    /* Reply status and headers */
    String statusLine = "";
    String headers = "";
    static int responseSize = 0;
    static String responseStatusCode = null;
    static String responseStatus = null;


    /* Body of reply */
    byte[] body = new byte[MAX_OBJECT_SIZE];


    /* Read response from server */
    public HttpResponse(DataInputStream fromServer)
    {

        /* Length of the object */
        int length = -1;
        boolean gotStatusLine = false;

        /* First read status line and Response Headers */
        try
        {
            String line = fromServer.readLine(); /* Read inputStream from server */
            responseSize = 0;

            while (line.length() != 0)
            {
                if (!gotStatusLine)
                {
                    statusLine = line;
                    gotStatusLine = true;
                    responseStatus = statusLine;
                }
                else
                {
                    headers += line + CRLF;
                }

                //Get File Length
                if (line.startsWith("Content-Length:") || line.startsWith("Content-length:"))
                {
                    String[] tmp = line.split(" ");
                    length = Integer.parseInt(tmp[1]);
                }

                line = fromServer.readLine();

            }
        }
        catch (Exception e)
        {
        }


        /* Response Body */
        try
        {
            int bytesRead = 0;
            byte buf[] = new byte[BUF_SIZE];
            boolean loop = false;

	        /*
	            If we didn't get Content-Length header,
	            just loop until the connection is closed.
	        */
            if (length == -1)
            {
                loop = true;
            }

      
            while (bytesRead < length || loop)
            {
                /* Read it in as binary data */
                int res = fromServer.read(buf, 0, BUF_SIZE); /* Reads binary data up to specify BUFF_SIZE */
                if (res == -1)
                {
                    break;
                }

                /* Copy the bytes into body. Make sure we don't exceed the maximum object size. */
                for (int i = 0; i < res && (i + bytesRead) < MAX_OBJECT_SIZE; i++)
                {
                    body[bytesRead + i] = buf[i]; /* copy bytes read to body */
                }
                bytesRead += res;
                responseSize = bytesRead;
            }
        }
        catch (IOException e)
        {
            return;
        }
        catch (Exception e)
        {
        }


    }

    /*
        Convert response into a string for easy re-sending.
        Only converts the response headers, body is not converted to a string.
    */
    public String toString()
    {

        /* Working with Response Header here */
        String res = statusLine + CRLF;
        res += headers;
        responseStatusCode = statusLine.substring(9, statusLine.length()); //Get status code for log entry

        res += CRLF;
        return res;
    }
}