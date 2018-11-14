package jp.dodododo.dao.impl;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Dept {
	private String DEPTNO;

	private String DNAME;

	private String LOC;

	private String VERSIONNO;

	public String getDEPTNO() {
		return DEPTNO;
	}

	public void setDEPTNO(String deptno) {
		DEPTNO = deptno;
	}

	public String getDNAME() {
		return DNAME;
	}

	public void setDNAME(String dname) {
		DNAME = dname;
	}

	public String getLOC() {
		return LOC;
	}

	public void setLOC(String loc) {
		LOC = loc;
	}

	public String getVERSIONNO() {
		return VERSIONNO;
	}

	public void setVERSIONNO(String versionno) {
		VERSIONNO = versionno;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public boolean equals(Object o) {
		return EqualsBuilder.reflectionEquals(this, o);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

}
