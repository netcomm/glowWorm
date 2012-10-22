/*
 * Copyright 1999-2101 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.glowworm.serializer;

import java.io.IOException;
import java.io.Writer;

import com.jd.glowworm.util.BufferOutputStream;
import com.jd.glowworm.util.CodedOutputStream;

/**
 * @author wenshao<szujobs@hotmail.com>
 */
public final class SerializeWriter extends Writer {
    private CodedOutputStream theCodedOutputStream;
    
    /**
     * Creates a new CharArrayWriter with the specified initial size.
     * 
     * @param initialSize an int specifying the initial buffer size.
     * @exception IllegalArgumentException if initialSize is negative
     */
    public SerializeWriter(){
    	theCodedOutputStream = new CodedOutputStream(
    		new BufferOutputStream(1024 * 2));
    }

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	public void writeFieldValue_Byte(int value) {
		theCodedOutputStream.writeRawByte(0);
		theCodedOutputStream.writeRawByte(value);
	}
	
	public void writeFieldValue(int value) {
        //System.out.println("writeFieldValue: "+name+":"+value);
		//theCodedOutputStream.writeString(fieldInfoIndexParm, value);
		theCodedOutputStream.writeRawByte(0);
		theCodedOutputStream.writeInt32(value);
    }
	
	public void writeFieldValue(char value) {
        //System.out.println("writeFieldValue: "+name+":"+value);
		//theCodedOutputStream.writeString(fieldInfoIndexParm, value);
		theCodedOutputStream.writeRawByte(0);
		theCodedOutputStream.writeString(new Character(value).toString());
    }
	
	public void writeFieldValue(boolean value) {
        //System.out.println("writeFieldValue: "+name+":"+value);
		//theCodedOutputStream.writeString(fieldInfoIndexParm, value);
		theCodedOutputStream.writeRawByte(0);
		theCodedOutputStream.writeRawByte(value ? 1 : 0);
    }
	
	public void writeFieldValue(long value)
	{
		theCodedOutputStream.writeRawByte(0);
		theCodedOutputStream.writeRawVarint64(value);
	}
	
	public void writeFieldValue(float value)
	{
		theCodedOutputStream.writeRawByte(0);
		writeFloat(value);
	}
	
	public void writeFieldValue(String name, String value, int fieldInfoIndexParm) {
        //System.out.println("writeFieldValue: "+name+":"+value);
		//theCodedOutputStream.writeString(fieldInfoIndexParm, value);
		theCodedOutputStream.writeRawByte(0);
		theCodedOutputStream.writeString(value);
    }
	
	public void writeFieldValue(double valueParm) {
        //System.out.println("writeFieldValue: "+name+":"+value);
		//theCodedOutputStream.writeString(fieldInfoIndexParm, value);
		theCodedOutputStream.writeRawByte(0);
		theCodedOutputStream.writeDouble(valueParm);
    }
	
	public void writeFieldName(String name, int fieldInfoIndexParm) {
		//System.out.println("writeFieldName: "+name+":"+name);
		theCodedOutputStream.writeString(fieldInfoIndexParm, name);
    }
	
	public void writeGroup(int fieldInfoIndexParm, int groupItemCntParm) {
		theCodedOutputStream.writeRawByte(0);
		theCodedOutputStream.writeGroup(fieldInfoIndexParm, groupItemCntParm);
    }
	
	public void writeNull()
	{
		theCodedOutputStream.writeRawByte(1);
	}
	
	public void writeString(String valueParm)
	{
		theCodedOutputStream.writeString(valueParm);
	}
	
	public void writeInt(int valueParm)
	{
		theCodedOutputStream.writeInt32(valueParm);
	}
	
	public void writeFloat(float valueParm)
	{
		theCodedOutputStream.writeRawLittleEndian32
			(Float.floatToRawIntBits(valueParm));
	}
	
	public void writeDouble(double valueParm)
	{
		theCodedOutputStream.writeDouble(valueParm);
	}
	
	public void writeBool(boolean valueParm)
	{
		theCodedOutputStream.writeRawByte(valueParm ? 1 : 0);
	}
	
	public void writeByte(byte valueParm)
	{
		theCodedOutputStream.writeRawByte(valueParm);
	}
	
	public void writeLong(long valueParm)
	{
		theCodedOutputStream.writeRawVarint64(valueParm);
	}
	
	public CodedOutputStream getCodedOutputStream()
	{
		return theCodedOutputStream;
	}
	
	public void writeFieldNull() {
		theCodedOutputStream.writeRawByte(1);
    }
	
	public void writeIntArray(int[] array) {
		writeInt(array.length);
        for (int i = 0; i < array.length; ++i) {
            int val = array[i];
            writeInt(val);
        }
    }
	
	public void writeBooleanArray(boolean[] array)
	{
		//writeInt(array.length);
		byte[] tmpBytes = new byte[array.length];
        for (int i = 0; i < array.length; ++i)
        {
            if (array[i] == true)
            {
            	tmpBytes[i] = 1;
            }
            else
            {
            	tmpBytes[i] = 0;
            }
        }
        
        writeByteArray(tmpBytes);
	}
	
	public void writeDoubleArray(double[] array)
	{
		writeInt(array.length);
        for (int i = 0; i < array.length; ++i) {
            double val = array[i];
            writeDouble(val);
        }
	}
	
	public void writeFloatArray(float[] array)
	{
		writeInt(array.length);
        for (int i = 0; i < array.length; ++i) {
            writeFloat(array[i]);
        }
	}
	
	public void writeLongArray(long[] array) {
		writeInt(array.length);
        for (int i = 0; i < array.length; ++i) {
            writeLong(array[i]);
        }
    }
	
	public void writeShortArray(short[] array) {
		writeInt(array.length);
        for (int i = 0; i < array.length; ++i) {
            writeInt(array[i]);
        }
    }
	
	public void writeByteArray(byte[] array) {
		writeInt(array.length);
		theCodedOutputStream.writeRawBytes(array);
    }
}
