package io.mycat.proxy.man;

import java.io.IOException;
import java.nio.ByteBuffer;

import io.mycat.proxy.ProxyBuffer;

/**
 * 管理报文，3个字节的包头 ，其中前两个字节为包长度，第3个字节为包类型，最后为报文内容。
 * 
 * @author wuzhihui
 *
 */
public abstract class ManagePacket {
	// 两字节的报文长度+1字节的类型
	public final static int packetHeaderSize = 3;

	// 所有的报文类型都需要在这里统一声明
	public static final byte PKG_FAILED = 0;
	public static final byte PKG_SUCCESS = 1;
	public static final byte PKG_NODE_REG = 2;
	public static final byte PKG_JOIN_REQ_ClUSTER = 3;
	public static final byte PKG_JOIN_NOTIFY_ClUSTER = 4;
	public static final byte PKG_JOIN_ACK_ClUSTER = 5;

	public static final byte PKG_CONFIG_VERSION_REQ = 6;
	public static final byte PKG_CONFIG_VERSION_RES = 7;
	public static final byte PKG_CONFIG_REQ = 8;
	public static final byte PKG_CONFIG_RES = 9;

	public static final byte PKG_CONFIG_PREPARE = 10;
	public static final byte PKG_CONFIG_CONFIRM = 11;
	public static final byte PKG_CONFIG_COMMIT = 12;

	public static final byte PKG_LEADER_NOTIFY = 13;

	protected byte pkgType;
	// 长度最长为2字节的short，即65535，长度包括包头3个字节在内
	protected int pkgLength;

	public static final String getTypeString(byte type) {
		String str = "";
		switch (type) {
		case 0:
			str = "PKG_FAILED";
			break;
		case 1:
			str = "PKG_SUCCESS";
			break;
		case 2:
			str = "PKG_NODE_REG";
			break;
		case 3:
			str = "PKG_JOIN_REQ_ClUSTER";
			break;
		case 4:
			str = "PKG_JOIN_NOTIFY_ClUSTER";
			break;
		case 5:
			str = "PKG_JOIN_ACK_ClUSTER";
			break;
		case 6:
			str = "PKG_CONFIG_VERSION_REQ";
			break;
		case 7:
			str = "PKG_CONFIG_VERSION_RES";
			break;
		case 8:
			str = "PKG_CONFIG_REQ";
			break;
		case 9:
			str = "PKG_CONFIG_RES";
			break;
		case 10:
			str = "PKG_CONFIG_PREPARE";
			break;
		case 11:
			str = "PKG_CONFIG_CONFIRM";
			break;
		case 12:
			str = "PKG_CONFIG_COMMIT";
			break;
		case 13:
			str = "PKG_LEADER_NOTIFY";
			break;
		default:
			break;
		}
		return str;
	}

	public ManagePacket(byte pkgType) {
		this.pkgType = pkgType;
	}

	public static final boolean validateHeader(final long offset, final long position) {
		return offset + packetHeaderSize <= position;

	}

	/**
	 * 获取报文长度
	 * 
	 * @param buffer
	 *            报文buffer
	 * @param offset
	 *            buffer解析位置偏移量
	 * @return 报文长度(Header长度+内容长度)
	 * @throws IOException
	 */
	public static final int getPacketLength(ByteBuffer buffer, int offset) throws IOException {
		int length = buffer.get(offset) & 0xff;
		length |= (buffer.get(++offset) & 0xff) << 8;
		return length + packetHeaderSize;
	}

	public byte getPkgType() {
		return pkgType;
	}

	public void setPkgType(byte pkgType) {
		this.pkgType = pkgType;
	}

	public int getPkgLength() {
		return pkgLength;
	}

	public void setPkgLength(int pkgLength) {
		this.pkgLength = pkgLength;
	}

	public void resolve(ProxyBuffer buffer) {
		buffer.skip(3);
		this.resolveBody(buffer);
	}

	public abstract void resolveBody(ProxyBuffer buffer);

	/**
	 * 报文内容写入到Buffer中（等待发送）
	 * 
	 * @param buffer
	 */
	public void writeTo(ProxyBuffer buffer) {
		int beginPos = buffer.writeIndex;
		buffer.writeIndex=2;
		buffer.writeByte(this.pkgType);
		this.writeBody(buffer);
		// total length
		int lastPos = buffer.writeIndex;
		buffer.writeIndex = beginPos;
		buffer.writeFixInt(2, lastPos - packetHeaderSize);
		buffer.writeIndex = lastPos;
		buffer.readIndex  = buffer.writeIndex;
	}

	/**
	 * 需要保证内容不超过buffer的容量
	 * 
	 * @param buffer
	 */
	public abstract void writeBody(ProxyBuffer buffer);
}
