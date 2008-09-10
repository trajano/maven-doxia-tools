package org.apache.maven.doxia;

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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;

import org.apache.maven.doxia.wrapper.InputFileWrapper;
import org.apache.maven.doxia.wrapper.InputReaderWrapper;
import org.apache.maven.doxia.wrapper.OutputFileWrapper;
import org.apache.maven.doxia.wrapper.OutputWriterWrapper;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.WriterFactory;

/**
 * Tests Doxia converter.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id$
 */
public class ConverterTest
    extends PlexusTestCase
{
    Converter converter;

    /** {@inheritDoc} */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        converter = new DefaultConverter();
    }

    /** {@inheritDoc} */
    protected void tearDown()
        throws Exception
    {
        super.tearDown();

        converter = null;
    }

    /**
     * @see Converter#getInputFormats()
     */
    public void testGetInputFormats()
    {
        assertNotNull( converter.getInputFormats() );
    }

    /**
     * @see Converter#getOutputFormats()
     */
    public void testGetOutputFormats()
    {
        assertNotNull( converter.getOutputFormats() );
    }

    /**
     * Input file / output dir
     *
     * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
     * @throws Exception if any
     */
    public void testFileConverterWithInputFileOutputDir()
        throws Exception
    {
        String in = getBasedir() + "/src/test/resources/unit/Doxia.htm";
        String from = "xhtml";
        String out = getBasedir() + "/target/unit/";
        String to = "apt";

        InputFileWrapper input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
        OutputFileWrapper output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

        converter.convert( input, output );
        assertTrue( new File( out, "Doxia.htm.apt" ).exists() );

        FileUtils.deleteDirectory( new File( getBasedir() + "/target/unit/" ) );
    }

    /**
     * Input file / output file
     *
     * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
     * @throws Exception if any
     */
    public void testFileConverterWithInputFileOutputFile()
        throws Exception
    {
        String in = getBasedir() + "/src/test/resources/unit/Doxia.htm";
        String from = "xhtml";
        String out = getBasedir() + "/target/unit/Doxia.apt";
        String to = "apt";

        InputFileWrapper input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
        OutputFileWrapper output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

        converter.convert( input, output );
        assertTrue( new File( out ).exists() );

        FileUtils.deleteDirectory( new File( getBasedir() + "/target/unit/" ) );
    }

    /**
     * Input apt file / output file
     *
     * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
     * @throws Exception if any
     */
    public void testAptFileConverter()
        throws Exception
    {
        String in = getBasedir() + "/src/test/resources/unit/apt/test.apt";
        String from = "apt";
        String out = getBasedir() + "/target/unit/file/apt/test.apt.xhtml";
        String to = "xhtml";

        InputFileWrapper input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
        OutputFileWrapper output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

        converter.convert( input, output );
        assertTrue( new File( out ).exists() );

        in = getBasedir() + "/target/unit/file/apt/test.apt.xhtml";
        from = "xhtml";
        out = getBasedir() + "/target/unit/file/apt/test.apt";
        to = "apt";

        input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
        output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

        converter.convert( input, output );
        assertTrue( new File( out ).exists() );
    }

    /**
     * Input confluence file / output file
     *
     * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
     * @throws Exception if any
     */
    public void testConfluenceFileConverter()
        throws Exception
    {
        String in = getBasedir() + "/src/test/resources/unit/confluence/test.confluence";
        String from = "confluence";
        String out = getBasedir() + "/target/unit/file/confluence/test.confluence.xhtml";
        String to = "xhtml";

        InputFileWrapper input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
        OutputFileWrapper output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

        converter.convert( input, output );
        assertTrue( new File( out ).exists() );

        in = getBasedir() + "/target/unit/file/confluence/test.confluence.xhtml";
        from = "xhtml";
        out = getBasedir() + "/target/unit/file/confluence/test.confluence";
        to = "confluence";

        try
        {
            input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
            output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

            converter.convert( input, output );

            assertFalse( true );
        }
        catch ( UnsupportedFormatException e )
        {
            assertTrue( true );
        }
    }

    /**
     * Input docbook file / output file
     *
     * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
     * @throws Exception if any
     */
    public void testDocbookFileConverter()
        throws Exception
    {
        String in = getBasedir() + "/src/test/resources/unit/docbook/test.xml";
        String from = "docbook";
        String out = getBasedir() + "/target/unit/file/docbook/test.docbook.xhtml";
        String to = "xhtml";

        InputFileWrapper input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
        OutputFileWrapper output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

        converter.convert( input, output );
        assertTrue( new File( out ).exists() );

        // TODO: docbook output is still crap, see DOXIA-184
        //        in = getBasedir() + "/target/unit/file/docbook/test.docbook.xhtml";
        //        from = "xhtml";
        //        out = getBasedir() + "/target/unit/file/docbook/test.docbook";
        //        to = "docbook";
        //
        //        input = InputFileWrapper.valueOf( in, from, converter.getInputFormats() );
        //        output = OutputFileWrapper.valueOf( out, to, converter.getOutputFormats() );
        //
        //        converter.convert( input, output );
        //        assertTrue( new File( out ).exists() );
    }

    /**
     * Input fml dir / output dir
     *
     * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
     * @throws Exception if any
     */
    public void testFmlFileConverter()
        throws Exception
    {
        String in = getBasedir() + "/src/test/resources/unit/fml/test.fml";
        String from = "fml";
        String out = getBasedir() + "/target/unit/file/fml/test.fml.xhtml";
        String to = "xhtml";

        InputFileWrapper input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
        OutputFileWrapper output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

        converter.convert( input, output );
        assertTrue( new File( out ).exists() );

        in = getBasedir() + "/target/unit/file/fml/test.fml.xhtml";
        from = "xhtml";
        out = getBasedir() + "/target/unit/file/fml/test.fml";
        to = "fml";

        try
        {
            input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
            output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

            converter.convert( input, output );

            assertFalse( true );
        }
        catch ( UnsupportedFormatException e )
        {
            assertTrue( true );
        }
    }

    /**
     * Input twiki file / output file
     *
     * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
     * @throws Exception if any
     */
    public void testTwikiFileConverter()
        throws Exception
    {
        String in = getBasedir() + "/src/test/resources/unit/twiki/test.twiki";
        String from = "twiki";
        String out = getBasedir() + "/target/unit/file/twiki/test.twiki.xhtml";
        String to = "xhtml";

        InputFileWrapper input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
        OutputFileWrapper output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

        converter.convert( input, output );
        assertTrue( new File( out ).exists() );

        in = getBasedir() + "/target/unit/file/twiki/test.twiki.xhtml";
        from = "xhtml";
        out = getBasedir() + "/target/unit/file/twiki/test.twiki";
        to = "twiki";

        try
        {
            input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
            output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

            converter.convert( input, output );

            assertFalse( true );
        }
        catch ( UnsupportedFormatException e )
        {
            assertTrue( true );
        }
    }

    /**
     * Input xdoc file / output dir
     *
     * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
     * @throws Exception if any
     */
    public void testXdocFileConverter()
        throws Exception
    {
        String in = getBasedir() + "/src/test/resources/unit/xdoc/test.xml";
        String from = "xdoc";
        String out = getBasedir() + "/target/unit/file/xdoc/test.xdoc.xhtml";
        String to = "xhtml";

        InputFileWrapper input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
        OutputFileWrapper output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

        converter.convert( input, output );
        assertTrue( new File( out ).exists() );

        in = getBasedir() + "/target/unit/file/xdoc/test.xdoc.xhtml";
        from = "xhtml";
        out = getBasedir() + "/target/unit/file/xdoc/test.xdoc";
        to = "xdoc";

        input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
        output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

        converter.convert( input, output );
        assertTrue( new File( out ).exists() );
    }

    /**
     * Input xhtml file / output dir
     *
     * @see Converter#convert(InputFileWrapper, OutputFileWrapper)
     * @throws Exception if any
     */
    public void testXhtmlFileConverter()
        throws Exception
    {
        String in = getBasedir() + "/src/test/resources/unit/xhtml/test.xhtml";
        String from = "xhtml";
        String out = getBasedir() + "/target/unit/file/xhtml/test.xhtml.xhtml";
        String to = "xhtml";

        InputFileWrapper input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
        OutputFileWrapper output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

        converter.convert( input, output );
        assertTrue( new File( out ).exists() );

        in = getBasedir() + "/target/unit/file/xhtml/test.xhtml.xhtml";
        from = "xhtml";
        out = getBasedir() + "/target/unit/file/xhtml/test.xhtml";
        to = "xhtml";

        input = InputFileWrapper.valueOf( in, from, ReaderFactory.UTF_8, converter.getInputFormats() );
        output = OutputFileWrapper.valueOf( out, to, WriterFactory.UTF_8, converter.getOutputFormats() );

        converter.convert( input, output );
        assertTrue( new File( out ).exists() );
    }

    /**
     * Input apt reader / output writer
     *
     * @see Converter#convert(InputReaderWrapper, OutputWriterWrapper)
     * @throws Exception if any
     */
    public void testAptWriterConverter()
        throws Exception
    {
        String in = getBasedir() + "/src/test/resources/unit/apt/test.apt";
        String from = "apt";
        String out = getBasedir() + "/target/unit/writer/apt/test.apt.xhtml";
        String to = "xhtml";

        File inFile = new File( in );
        File outFile = new File( out );
        outFile.getParentFile().mkdirs();
        FileWriter fw = new FileWriter( outFile );

        StringWriter writer = new StringWriter();

        InputReaderWrapper input = InputReaderWrapper.valueOf( new FileReader( inFile ), from, converter
            .getInputFormats() );
        OutputWriterWrapper output = OutputWriterWrapper.valueOf( writer, to, converter
            .getOutputFormats() );

        converter.convert( input, output );

        IOUtil.copy( writer.toString(), fw );
        IOUtil.close( fw );

        assertTrue( outFile.exists() );
    }

    /**
     * Input confluence reader / output writer
     *
     * @see Converter#convert(InputReaderWrapper, OutputWriterWrapper)
     * @throws Exception if any
     */
    public void testConfluenceWriterConverter()
        throws Exception
    {
        String in = getBasedir() + "/src/test/resources/unit/confluence/test.confluence";
        String from = "confluence";
        String out = getBasedir() + "/target/unit/writer/confluence/test.confluence.xhtml";
        String to = "xhtml";

        File inFile = new File( in );
        File outFile = new File( out );
        outFile.getParentFile().mkdirs();
        FileWriter fw = new FileWriter( outFile );

        StringWriter writer = new StringWriter();

        InputReaderWrapper input = InputReaderWrapper.valueOf( new FileReader( inFile ), from, converter
            .getInputFormats() );
        OutputWriterWrapper output = OutputWriterWrapper.valueOf( new FileWriter( outFile ), to, converter
            .getOutputFormats() );

        converter.convert( input, output );

        IOUtil.copy( writer.toString(), fw );
        IOUtil.close( fw );

        assertTrue( outFile.exists() );
    }
}
