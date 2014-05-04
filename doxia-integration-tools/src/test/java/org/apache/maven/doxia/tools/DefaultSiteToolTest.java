package org.apache.maven.doxia.tools;

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

import junit.framework.TestCase;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id$
 */
public class DefaultSiteToolTest
    extends TestCase
{
    /**
     * test getNormalizedPath().
     */
    public void testGetNormalizedPath()
    {
        assertEquals( null, DefaultSiteTool.getNormalizedPath( null ) );
        assertEquals( "", DefaultSiteTool.getNormalizedPath( "" ) );
        assertEquals( "", DefaultSiteTool.getNormalizedPath( "." ) );
        assertEquals( "", DefaultSiteTool.getNormalizedPath( "./" ) );
        assertEquals( "foo", DefaultSiteTool.getNormalizedPath( "foo" ) );
        assertEquals( "foo/bar", DefaultSiteTool.getNormalizedPath( "foo/bar" ) );
        assertEquals( "foo/bar", DefaultSiteTool.getNormalizedPath( "foo\\bar" ) );
        assertEquals( "foo/bar", DefaultSiteTool.getNormalizedPath( "foo/./bar" ) );
        assertEquals( "foo/bar", DefaultSiteTool.getNormalizedPath( "foo//bar" ) );
        assertEquals( "", DefaultSiteTool.getNormalizedPath( "foo/../" ) );
        assertEquals( "", DefaultSiteTool.getNormalizedPath( "foo/.." ) );
        assertEquals( "bar", DefaultSiteTool.getNormalizedPath( "foo/../bar" ) );
        assertEquals( "foo", DefaultSiteTool.getNormalizedPath( "./foo" ) );
        assertEquals( "../foo", DefaultSiteTool.getNormalizedPath( "../foo" ) );
        assertEquals( "../../foo", DefaultSiteTool.getNormalizedPath( "../../foo" ) );
        assertEquals( "index.html", DefaultSiteTool.getNormalizedPath( "./foo/../index.html" ) );

        // note: space is preserved and double slash is removed!
        assertEquals( "file:/Documents and Settings/", DefaultSiteTool.getNormalizedPath( "file://Documents and Settings/" ) );
    }

    public void testGetRelativePath()
    {
        SiteTool siteTool = new DefaultSiteTool();
        assertEquals( "..", siteTool.getRelativePath("www", "www/slashdot"));
        assertEquals( "../..", siteTool.getRelativePath("www", "www/slashdot/foo"));
        assertEquals( "slashdot", siteTool.getRelativePath("www/slashdot", "www"));
    }
}
