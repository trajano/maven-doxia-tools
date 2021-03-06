package org.apache.maven.doxia.linkcheck.validation;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.net.ssl.SSLHandshakeException;

import org.apache.maven.doxia.linkcheck.DefaultLinkCheck2;
import org.apache.maven.doxia.linkcheck.HttpBean;
import org.apache.maven.doxia.linkcheck.model.LinkcheckFileResult;
import org.codehaus.plexus.util.Base64;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks links which are normal URLs using {@link HttpURLConnection}.
 *
 * @author <a href="mailto:bwalding@apache.org">Ben Walding</a>
 * @author <a href="mailto:aheritier@apache.org">Arnaud Heritier</a>
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id$
 */
public final class HttpURLConnectionLinkValidator
    extends HTTPLinkValidator
{
    /** Log for debug output. */
	private static final Logger LOG = LoggerFactory.getLogger( DefaultLinkCheck2.class );

    /** Use the get method to test pages. */
    private static final String GET_METHOD = "get";

    /** Use the head method to test pages. */
    private static final String HEAD_METHOD = "head";

    /**
     * Temporary Redirect HTTP status code.  From
     * org.apache.commons.httpclient.HttpStatus#SC_TEMPORARY_REDIRECT
     */
    private static final int SC_TEMPORARY_REDIRECT = 307;

    /**
     * Maximum number of redirects before stopping.
     */
    private static final int  MAX_REDIRECT_COUNT = 20;

    /** The http bean encapsulating all http parameters supported. */
    private HttpBean http;

    /** The base URL for links that start with '/'. */
    private String baseURL;

    /**
     * Constructor: initialize settings, use "head" method.
     */
    public HttpURLConnectionLinkValidator()
    {
        this( new HttpBean() );
    }

    /**
     * Constructor: initialize settings.
     *
     * @param bean The http bean encapsuling all HTTP parameters supported.
     */
    public HttpURLConnectionLinkValidator( HttpBean bean )
    {
        if ( bean == null )
        {
            bean = new HttpBean();
        }

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( "Will use method : [" + bean.getMethod() + "]" );
        }

        this.http = bean;
        
    }

    /**
     * The base URL.
     *
     * @return the base URL.
     */
    public String getBaseURL()
    {
        return this.baseURL;
    }

    /**
     * Sets the base URL. This is pre-pended to links that start with '/'.
     *
     * @param url the base URL.
     */
    public void setBaseURL( String url )
    {
        this.baseURL = url;
    }

    /** {@inheritDoc} */
    public LinkValidationResult validateLink( final LinkValidationItem lvi )
    {

        String link = lvi.getLink();
        HttpURLConnection conn = null;

        try
        {
            URI linkURI;
            if ( link.startsWith( "/" ) )
            {
                if ( getBaseURL() == null )
                {
                    if ( LOG.isWarnEnabled() )
                    {
                        LOG.warn( "Cannot check link [" + link + "] in page [" + lvi.getSource()
                            + "], as no base URL has been set!" );
                    }

                    return new LinkValidationResult( LinkcheckFileResult.WARNING_LEVEL, false,
                                                     "No base URL specified" );
                }

                linkURI = URI.create( getBaseURL() ).resolve( link );
            }
            else
            {
                linkURI = new URI( link );
            }

            int redirectCount = 0;
            String content = null;
            String cookies = null;
            try
            {
                do {
                    final URL url = linkURI.toURL();
                    conn = (HttpURLConnection) url.openConnection(buildProxyForUri(linkURI));
                    // Handle HTTP proxy authentication if provided.
                    if (http.getProxyUser() != null && http.getProxyPassword() != null)
                    {
                    	conn.setRequestProperty("Proxy-Authorization", 
                            "Basic " + new String( Base64.encodeBase64( ( http.getProxyUser() + http.getProxyPassword()).getBytes() ) ) );
                    }
                    conn.setInstanceFollowRedirects(false);
                    final boolean hasContentExpected;
                    if ( HEAD_METHOD.equalsIgnoreCase( this.http.getMethod() ) )
                    {
                        conn.setRequestMethod( "HEAD" );
                        hasContentExpected = false;
                    }
                    else if ( GET_METHOD.equalsIgnoreCase( this.http.getMethod() ) )
                    {
                        conn.setRequestMethod( "GET" );
                        hasContentExpected = true;
                    }
                    else
                    {
                        if ( LOG.isErrorEnabled() )
                        {
                            LOG.error( "Unsupported method: " + this.http.getMethod() + ", using 'get'." );
                        }
                        conn.setRequestMethod( "GET" );
                        hasContentExpected = true;
                    }

                    if ( cookies != null )
                    {
                        conn.setRequestProperty("Cookie", cookies);
                    }

                    conn.setConnectTimeout( http.getTimeout() );
                    conn.connect();
                    if ( isHttpError( conn.getResponseCode() ) ) {
                        return new HTTPLinkValidationResult( LinkcheckFileResult.ERROR_LEVEL, false, conn.getResponseCode(),
                                    conn.getResponseMessage() );
                    }
                    else if ( isRedirect( conn.getResponseCode() ) )
                    {
                        linkURI = URI.create( conn.getHeaderField( "Location" ) );
                        LOG.debug(url.toString() + " is redirecting to " + linkURI);
                        cookies = conn.getHeaderField( "Set-Cookie" );
                        ++redirectCount;
                    }
                    else if ( hasContentExpected )
                    {
                        content = IOUtil.toString( conn.getInputStream() );
                    }
                    else
                    {
                        conn.getInputStream();
                    }
                    conn.disconnect();
                }
                while ( redirectCount < MAX_REDIRECT_COUNT && isRedirect( conn.getResponseCode() ) && http.isFollowRedirects());
            }
            catch ( final SSLHandshakeException t )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Received: [" + t + "] for [" + link + "] in page [" + lvi.getSource() + "]", t );
                }
                validateCryptographyForUrl( linkURI );
                return new LinkValidationResult( LinkcheckFileResult.ERROR_LEVEL, false, t.getClass().getName()
                        + " : " + t.getMessage() );
            }
            catch ( Throwable t )
            {
                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( "Received: [" + t + "] for [" + link + "] in page [" + lvi.getSource() + "]", t );
                }

                return new LinkValidationResult( LinkcheckFileResult.ERROR_LEVEL, false, t.getClass().getName()
                    + " : " + t.getMessage() );
            }

            if ( conn.getResponseCode() == HttpURLConnection.HTTP_OK )
            {
                // lets check if the anchor is present
                if ( linkURI.getFragment() != null && content != null )
                {

                    if ( !Anchors.matchesAnchor( content, linkURI.getFragment() ) )
                    {
                        return new HTTPLinkValidationResult( LinkcheckFileResult.VALID_LEVEL, false,
                            "Missing anchor '" + linkURI.getFragment() + "'" );
                    }
                }

                return new HTTPLinkValidationResult( LinkcheckFileResult.VALID_LEVEL, true, conn.getResponseCode(),
                                                     conn.getResponseMessage() );
            }

            String msg =
                "Received: [" + conn.getResponseCode() + "] for [" + link + "] in page [" + lvi.getSource() + "]";
            // If there's a redirection ... add a warning
            if ( isRedirect( conn.getResponseCode() ) )
            {
                LOG.warn( msg );

                return new HTTPLinkValidationResult( LinkcheckFileResult.WARNING_LEVEL, true, conn.getResponseCode(),
                    conn.getResponseMessage() );
            }

            LOG.debug( msg );

            return new HTTPLinkValidationResult( LinkcheckFileResult.ERROR_LEVEL, false, conn.getResponseCode(),
                conn.getResponseMessage() );
        }
        catch ( Throwable t )
        {
            String msg = "Received: [" + t + "] for [" + link + "] in page [" + lvi.getSource() + "]";
            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( msg, t );
            }
            else
            {
                LOG.error( msg );
            }

            return new LinkValidationResult( LinkcheckFileResult.ERROR_LEVEL, false, t.getMessage() );
        }
    }

    /**
     * This will build the proxy for a given URI.  At the moment only HTTP proxies are supported.
     * Also exclusion lists are not supported.
     * 
     * @param linkURI URI to connect to
     * @return proxy
     */
    private Proxy buildProxyForUri(final URI linkURI) {
    	if (http.getProxyHost() != null && http.getProxyPort() != 0)
    	{
    		return new Proxy(Type.HTTP, new InetSocketAddress(http.getProxyHost(), http.getProxyPort()));
    	}
		return Proxy.NO_PROXY;
	}

	/**
     * Checks if the URL has an HTTPS scheme and put out a warning when the cryptography extensions are not installed
     *
     * @param responseCode from {@link HttpURLConnection#getResponseCode()}
     * @return <code>true</code> if the response is an HTTP client or server error.
     * @throws NoSuchAlgorithmException Thrown if AES was not found (should never happen)
     */
    private void validateCryptographyForUrl(final URI uri) throws NoSuchAlgorithmException {
        if ( "https".equals(uri.getScheme() ) )
        {
            if (Cipher.getMaxAllowedKeyLength("AES") < Integer.MAX_VALUE)
            {
                LOG.warn( "The URL " + uri + " was requested and Java Cryptography Extensions Unlimited Policy is not installed.  This may cause handshake errors." );
            }
        }
    }

    /**
     * Checks if the HTTP response code is an error.  An error code is anything above 400 and 599.
     *
     * @param responseCode from {@link HttpURLConnection#getResponseCode()}
     * @return <code>true</code> if the response is an HTTP client or server error.
     */
    private boolean isHttpError( final int responseCode )
    {
        return responseCode >= 400 && responseCode <= 599;
    }
    
    /**
     * Checks if the HTTP response code is a redirect.  It also checks for 307 to make it compatible with the HTTP
     * client implementation.
     *
     * @param responseCode from {@link HttpURLConnection#getResponseCode()}
     * @return <code>true</code> if the response is an HTTP redirect.
     */
    private boolean isRedirect( final int responseCode )
    {
        return responseCode == HttpURLConnection.HTTP_MOVED_TEMP
            || responseCode == HttpURLConnection.HTTP_MOVED_PERM
            || responseCode == SC_TEMPORARY_REDIRECT;
    }
}
