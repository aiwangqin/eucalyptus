package com.eucalyptus.ws.binding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.util.StAXUtils;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallable;
import org.jibx.runtime.IMarshaller;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IXMLWriter;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.StAXWriter;

public class JiBXDataSource implements OMDataSource {
  private final int             marshallerIndex;
  private final String          elementName;
  private final String          elementNamespace;
  private final String          elementNamespacePrefix;
  private final int             elementNamespaceIndex;
  private final int[]           openNamespaceIndexes;
  private final String[]        openNamespacePrefixes;
  private final Object          dataObject;
  private final IBindingFactory bindingFactory;

  public JiBXDataSource( final IMarshallable obj, final IBindingFactory factory ) {
    this.marshallerIndex = -1;
    this.dataObject = obj;
    this.bindingFactory = factory;
    this.elementName = this.elementNamespace = this.elementNamespacePrefix = null;
    this.elementNamespaceIndex = -1;
    this.openNamespaceIndexes = null;
    this.openNamespacePrefixes = null;
  }

  public JiBXDataSource( final Object obj, final int index, final String name, final String uri, String prefix, int[] nsindexes, String[] nsprefixes, final IBindingFactory factory ) {
    if ( index < 0 ) { throw new IllegalArgumentException( "index value must be non-negative" ); }
    this.marshallerIndex = index;
    this.dataObject = obj;
    this.bindingFactory = factory;
    boolean found = false;
    final String[] nss = factory.getNamespaces( );
    int nsidx = -1;
    for ( int i = 0; i < nsindexes.length; i++ ) {
      if ( uri.equals( nss[nsindexes[i]] ) ) {
        nsidx = nsindexes[i];
        prefix = nsprefixes[i];
        found = true;
        break;
      }
    }
    this.elementName = name;
    this.elementNamespace = uri;
    this.elementNamespacePrefix = prefix;
    if ( !found ) {
      for ( int i = 0; i < nss.length; i++ ) {
        if ( uri.equals( nss[i] ) ) {
          nsidx = i;
          break;
        }
      }
      if ( nsidx >= 0 ) {
        final int[] icpy = new int[nsindexes.length + 1];
        icpy[0] = nsidx;
        System.arraycopy( nsindexes, 0, icpy, 1, nsindexes.length );
        nsindexes = icpy;
        final String[] scpy = new String[nsprefixes.length + 1];
        scpy[0] = prefix;
        System.arraycopy( nsprefixes, 0, scpy, 1, nsprefixes.length );
        nsprefixes = scpy;
      } else {
        throw new IllegalStateException( "Namespace not found" );
      }
    }
    this.elementNamespaceIndex = nsidx;
    this.openNamespaceIndexes = nsindexes;
    this.openNamespacePrefixes = nsprefixes;
  }

  private void marshal( final boolean full, final IMarshallingContext ctx ) throws JiBXException {
    try {

      if ( this.marshallerIndex < 0 ) {
        if ( this.dataObject instanceof IMarshallable ) {
          ( ( IMarshallable ) this.dataObject ).marshal( ctx );
        } else {
          throw new IllegalStateException( "Object of class " + this.dataObject.getClass( ).getName( ) + " needs a JiBX <mapping> to be marshalled" );
        }
      } else {
        final IXMLWriter wrtr = ctx.getXmlWriter( );
        String name = this.elementName;
        int nsidx = 0;
        if ( full ) {
          nsidx = this.elementNamespaceIndex;
          wrtr.startTagNamespaces( nsidx, name, this.openNamespaceIndexes, this.openNamespacePrefixes );
        } else {
          wrtr.openNamespaces( this.openNamespaceIndexes, this.openNamespacePrefixes );
          if ( !"".equals( this.elementNamespacePrefix ) ) {
            name = this.elementNamespacePrefix + ':' + name;
          }
          wrtr.startTagOpen( 0, name );
        }
        final IMarshaller mrsh = ctx.getMarshaller( this.marshallerIndex, this.bindingFactory.getMappedClasses( )[this.marshallerIndex] );
        mrsh.marshal( this.dataObject, ctx );
        wrtr.endTag( nsidx, name );
      }
      ctx.getXmlWriter( ).flush( );

    } catch ( final IOException e ) {
      throw new JiBXException( "Error marshalling XML representation: " + e.getMessage( ), e );
    }
  }

  public void serialize( final OutputStream output, final OMOutputFormat format ) throws XMLStreamException {
    try {
      final IMarshallingContext ctx = this.bindingFactory.createMarshallingContext( );
      ctx.setOutput( output, format == null ? null : format.getCharSetEncoding( ) );
      this.marshal( true, ctx );
    } catch ( final JiBXException e ) {
      throw new XMLStreamException( "Error in JiBX marshalling: " + e.getMessage( ), e );
    }
  }

  public void serialize( final Writer writer, final OMOutputFormat format ) throws XMLStreamException {
    try {
      final IMarshallingContext ctx = this.bindingFactory.createMarshallingContext( );
      ctx.setOutput( writer );
      this.marshal( true, ctx );
    } catch ( final JiBXException e ) {
      throw new XMLStreamException( "Error in JiBX marshalling: " + e.getMessage( ), e );
    }
  }

  public void serialize( final XMLStreamWriter xmlWriter ) throws XMLStreamException {
    try {
      boolean full = true;
      final String[] nss = this.bindingFactory.getNamespaces( );
      if ( this.marshallerIndex >= 0 ) {
        String prefix = xmlWriter.getPrefix( this.elementNamespace );
        if ( this.elementNamespacePrefix.equals( prefix ) ) {
          full = false;
          for ( int i = 0; i < this.openNamespaceIndexes.length; i++ ) {
            final String uri = nss[i];
            prefix = xmlWriter.getPrefix( uri );
            if ( !this.openNamespacePrefixes[i].equals( prefix ) ) {
              full = true;
              break;
            }
          }
        }
      }
      final IXMLWriter writer = new StAXWriter( nss, xmlWriter );
      final IMarshallingContext ctx = this.bindingFactory.createMarshallingContext( );
      ctx.setXmlWriter( writer );
      this.marshal( full, ctx );
    } catch ( final JiBXException e ) {
      throw new XMLStreamException( "Error in JiBX marshalling: " + e.getMessage( ), e );
    }
  }

  public XMLStreamReader getReader( ) throws XMLStreamException {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream( );
    this.serialize( bos, null );
    return StAXUtils.createXMLStreamReader( new ByteArrayInputStream( bos.toByteArray( ) ) );
  }
}