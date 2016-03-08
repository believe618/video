package com.coremedia.iso.boxes.threegpp26244;

import com.coremedia.iso.IsoTypeReader2;
import com.coremedia.iso.IsoTypeWriter2;
import com.coremedia.iso.Utf82;
import com.googlecode.mp4parser.AbstractFullBox2;

import java.nio.ByteBuffer;

/**
 * Location Information Box as specified in TS 26.244.
 */
public class LocationInformationBox2 extends AbstractFullBox2 {
    public static final String TYPE = "loci";

    private String language;
    private String name = "";
    private int role;
    private double longitude;
    private double latitude;
    private double altitude;
    private String astronomicalBody = "";
    private String additionalNotes = "";

    public LocationInformationBox2() {
        super(TYPE);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public String getAstronomicalBody() {
        return astronomicalBody;
    }

    public void setAstronomicalBody(String astronomicalBody) {
        this.astronomicalBody = astronomicalBody;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    protected long getContentSize() {
        return 22 + Utf82.convert(name).length + Utf82.convert(astronomicalBody).length + Utf82.convert(additionalNotes).length;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        language = IsoTypeReader2.readIso639(content);
        name = IsoTypeReader2.readString(content);
        role = IsoTypeReader2.readUInt8(content);
        longitude = IsoTypeReader2.readFixedPoint1616(content);
        latitude = IsoTypeReader2.readFixedPoint1616(content);
        altitude = IsoTypeReader2.readFixedPoint1616(content);
        astronomicalBody = IsoTypeReader2.readString(content);
        additionalNotes = IsoTypeReader2.readString(content);
    }


    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter2.writeIso639(byteBuffer, language);
        byteBuffer.put(Utf82.convert(name));
        byteBuffer.put((byte) 0);
        IsoTypeWriter2.writeUInt8(byteBuffer, role);
        IsoTypeWriter2.writeFixedPont1616(byteBuffer, longitude);
        IsoTypeWriter2.writeFixedPont1616(byteBuffer, latitude);
        IsoTypeWriter2.writeFixedPont1616(byteBuffer, altitude);
        byteBuffer.put(Utf82.convert(astronomicalBody));
        byteBuffer.put((byte) 0);
        byteBuffer.put(Utf82.convert(additionalNotes));
        byteBuffer.put((byte) 0);
    }
}
