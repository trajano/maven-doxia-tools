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

import java.io.File;

import org.apache.maven.doxia.linkcheck.HttpBean;
import org.apache.maven.doxia.linkcheck.model.LinkcheckFileResult;

import junit.framework.TestCase;

/**
 * @author <a href="bwalding@apache.org">Ben Walding</a>
 * @author <a href="aheritier@apache.org">Arnaud Heritier</a>
 * @version $Id$
 */
public class HTTPLinkValidatorTest extends TestCase
{
    private LinkValidator hlv;

    private boolean mavenOnline = true; // TODO: check if online

    public void testValidateLink() throws Exception
    {
        if ( this.mavenOnline )
        {

            this.hlv = new HttpURLConnectionLinkValidator();

            assertEquals( LinkcheckFileResult.VALID_LEVEL, checkLink( "http://www.apache.org" ).getStatus() );
            assertEquals( LinkcheckFileResult.ERROR_LEVEL, checkLink( "http://www.example.com>);" ).getStatus() );
        }
        else
        {
            this.hlv = new OfflineHTTPLinkValidator();

            assertEquals( LinkcheckFileResult.WARNING_LEVEL, checkLink( "http://www.apache.org" ).getStatus() );
            assertEquals( LinkcheckFileResult.WARNING_LEVEL, checkLink( "http://www.example.com>);" ).getStatus() );

        }
    }

    /**
     * Test some more public links.  Primarily to make sure some of the more problematic ones still work.
     */
    public void testValidateOtherInvalidLinks() throws Exception
    {
        final String[] urls = {
            "http://mojo.codehaus.org/build-helper-maven-plugin"
        };
        if ( this.mavenOnline )
        {
            this.hlv = new HttpURLConnectionLinkValidator();
            for (final String url : urls) 
            {
                assertEquals(url + " had wrong result", LinkcheckFileResult.ERROR_LEVEL, checkLink( url ).getStatus() );
            }
        }
        else
        {
            this.hlv = new OfflineHTTPLinkValidator();
            for (final String url : urls) 
            {
                assertEquals(url + " had wrong result", LinkcheckFileResult.WARNING_LEVEL, checkLink( url ).getStatus() );
            }
        }
    }


    /**
     * Tests a valid known redirect.
     */
    public void testValidateRedirectLink() throws Exception
    {
        if ( this.mavenOnline )
        {

            final HttpBean httpBean = new HttpBean();
            httpBean.setFollowRedirects(true);
            this.hlv = new HttpURLConnectionLinkValidator(httpBean);

            assertEquals( LinkcheckFileResult.VALID_LEVEL, checkLink( "http://site.trajano.net/trajano" ).getStatus() );

        }
        else
        {
            this.hlv = new OfflineHTTPLinkValidator();

            assertEquals( LinkcheckFileResult.WARNING_LEVEL, checkLink( "http://site.trajano.net/trajano" ).getStatus() );

        }
    }


    /**
     * Tests a valid known redirect.
     */
    public void testValidateRedirectLinkNotFollowing() throws Exception
    {
        if ( this.mavenOnline )
        {

            final HttpBean httpBean = new HttpBean();
            httpBean.setFollowRedirects(false);
            this.hlv = new HttpURLConnectionLinkValidator(httpBean);

            final HTTPLinkValidationResult checkLink = (HTTPLinkValidationResult)checkLink( "http://site.trajano.net/trajano" );
            assertEquals( LinkcheckFileResult.WARNING_LEVEL, checkLink.getStatus() );
            assertEquals( 301, checkLink.getHttpStatusCode() );

        }
        else
        {
            this.hlv = new OfflineHTTPLinkValidator();

            assertEquals( LinkcheckFileResult.WARNING_LEVEL, checkLink( "http://site.trajano.net/trajano" ).getStatus() );

        }
    }

    /**
     * Tests a valid known redirect using GET method.
     */
    public void testValidateRedirectLinkWithGet() throws Exception
    {
        if ( this.mavenOnline )
        {

            final HttpBean httpBean = new HttpBean();
            httpBean.setMethod("GET");
            httpBean.setFollowRedirects(true);
            this.hlv = new HttpURLConnectionLinkValidator(httpBean);

            assertEquals( LinkcheckFileResult.VALID_LEVEL, checkLink( "http://site.trajano.net/trajano" ).getStatus() );

        }
        else
        {
            this.hlv = new OfflineHTTPLinkValidator();

            assertEquals( LinkcheckFileResult.WARNING_LEVEL, checkLink( "http://site.trajano.net/trajano" ).getStatus() );

        }
    }

    /**
     * Tests 401.
     */
    public void test401() throws Exception
    {
        if ( this.mavenOnline )
        {

            this.hlv = new HttpURLConnectionLinkValidator();

            final HTTPLinkValidationResult result = (HTTPLinkValidationResult)checkLink( "https://oss.sonatype.org/service/local/staging/deploy/maven2/" );
            assertEquals( LinkcheckFileResult.ERROR_LEVEL, result.getStatus() );
            assertEquals( 401, result.getHttpStatusCode() );

        }
        else
        {
            this.hlv = new OfflineHTTPLinkValidator();

            assertEquals( LinkcheckFileResult.WARNING_LEVEL, checkLink( "https://oss.sonatype.org/service/local/staging/deploy/maven2/" ).getStatus() );

        }
    }

    /**
     * Tests no content type.
     */
    public void testNoContentType() throws Exception
    {
        if ( this.mavenOnline )
        {

            this.hlv = new HttpURLConnectionLinkValidator();

            assertEquals( LinkcheckFileResult.VALID_LEVEL, checkLink( "https://oss.sonatype.org/content/repositories/snapshots/" ).getStatus() );

        }
        else
        {
            this.hlv = new OfflineHTTPLinkValidator();

            assertEquals( LinkcheckFileResult.WARNING_LEVEL, checkLink( "https://oss.sonatype.org/content/repositories/snapshots/" ).getStatus() );

        }
    }

    protected LinkValidationResult checkLink( String link ) throws Exception
    {

        LinkValidationItem lvi = new LinkValidationItem( new File( "." ), link );
        return this.hlv.validateLink( lvi );
    }

}
