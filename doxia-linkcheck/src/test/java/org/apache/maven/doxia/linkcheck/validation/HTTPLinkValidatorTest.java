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

    protected LinkValidationResult checkLink( String link ) throws Exception
    {

        LinkValidationItem lvi = new LinkValidationItem( new File( "." ), link );
        return this.hlv.validateLink( lvi );
    }

}
