package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * ExternalPurpose1Code from ISO 20022.  Values from ISO 20022 External Code List ExternalCodeSets_1Q2018 June 2018. 
 */
public enum PurposeCode {
  
  BKDF("BKDF"),
  
  BKFE("BKFE"),
  
  BKFM("BKFM"),
  
  BKIP("BKIP"),
  
  BKPP("BKPP"),
  
  CBLK("CBLK"),
  
  CDCB("CDCB"),
  
  CDCD("CDCD"),
  
  CDCS("CDCS"),
  
  CDDP("CDDP"),
  
  CDOC("CDOC"),
  
  CDQC("CDQC"),
  
  ETUP("ETUP"),
  
  FCOL("FCOL"),
  
  MTUP("MTUP"),
  
  ACCT("ACCT"),
  
  CASH("CASH"),
  
  COLL("COLL"),
  
  CSDB("CSDB"),
  
  DEPT("DEPT"),
  
  INTC("INTC"),
  
  LIMA("LIMA"),
  
  NETT("NETT"),
  
  BFWD("BFWD"),
  
  CCIR("CCIR"),
  
  CCPC("CCPC"),
  
  CCPM("CCPM"),
  
  CCSM("CCSM"),
  
  CRDS("CRDS"),
  
  CRPR("CRPR"),
  
  CRSP("CRSP"),
  
  CRTL("CRTL"),
  
  EQPT("EQPT"),
  
  EQUS("EQUS"),
  
  EXPT("EXPT"),
  
  EXTD("EXTD"),
  
  FIXI("FIXI"),
  
  FWBC("FWBC"),
  
  FWCC("FWCC"),
  
  FWSB("FWSB"),
  
  FWSC("FWSC"),
  
  MARG("MARG"),
  
  MBSB("MBSB"),
  
  MBSC("MBSC"),
  
  MGCC("MGCC"),
  
  MGSC("MGSC"),
  
  OCCC("OCCC"),
  
  OPBC("OPBC"),
  
  OPCC("OPCC"),
  
  OPSB("OPSB"),
  
  OPSC("OPSC"),
  
  OPTN("OPTN"),
  
  OTCD("OTCD"),
  
  REPO("REPO"),
  
  RPBC("RPBC"),
  
  RPCC("RPCC"),
  
  RPSB("RPSB"),
  
  RPSC("RPSC"),
  
  RVPO("RVPO"),
  
  SBSC("SBSC"),
  
  SCIE("SCIE"),
  
  SCIR("SCIR"),
  
  SCRP("SCRP"),
  
  SHBC("SHBC"),
  
  SHCC("SHCC"),
  
  SHSL("SHSL"),
  
  SLEB("SLEB"),
  
  SLOA("SLOA"),
  
  SWBC("SWBC"),
  
  SWCC("SWCC"),
  
  SWPT("SWPT"),
  
  SWSB("SWSB"),
  
  SWSC("SWSC"),
  
  TBAS("TBAS"),
  
  TBBC("TBBC"),
  
  TBCC("TBCC"),
  
  TRCP("TRCP"),
  
  AGRT("AGRT"),
  
  AREN("AREN"),
  
  BEXP("BEXP"),
  
  BOCE("BOCE"),
  
  COMC("COMC"),
  
  CPYR("CPYR"),
  
  GDDS("GDDS"),
  
  GDSV("GDSV"),
  
  GSCB("GSCB"),
  
  LICF("LICF"),
  
  MP2B("MP2B"),
  
  POPE("POPE"),
  
  ROYA("ROYA"),
  
  SCVE("SCVE"),
  
  SERV("SERV"),
  
  SUBS("SUBS"),
  
  SUPP("SUPP"),
  
  TRAD("TRAD"),
  
  CHAR("CHAR"),
  
  COMT("COMT"),
  
  MP2P("MP2P"),
  
  ECPG("ECPG"),
  
  ECPR("ECPR"),
  
  ECPU("ECPU"),
  
  EPAY("EPAY"),
  
  CLPR("CLPR"),
  
  COMP("COMP"),
  
  DBTC("DBTC"),
  
  GOVI("GOVI"),
  
  HLRP("HLRP"),
  
  HLST("HLST"),
  
  INPC("INPC"),
  
  INPR("INPR"),
  
  INSC("INSC"),
  
  INSU("INSU"),
  
  INTE("INTE"),
  
  LBRI("LBRI"),
  
  LIFI("LIFI"),
  
  LOAN("LOAN"),
  
  LOAR("LOAR"),
  
  PENO("PENO"),
  
  PPTI("PPTI"),
  
  RELG("RELG"),
  
  RINP("RINP"),
  
  TRFD("TRFD"),
  
  FORW("FORW"),
  
  FXNT("FXNT"),
  
  ADMG("ADMG"),
  
  ADVA("ADVA"),
  
  BCDM("BCDM"),
  
  BCFG("BCFG"),
  
  BLDM("BLDM"),
  
  BNET("BNET"),
  
  CBFF("CBFF"),
  
  CBFR("CBFR"),
  
  CCRD("CCRD"),
  
  CDBL("CDBL"),
  
  CFEE("CFEE"),
  
  CGDD("CGDD"),
  
  CORT("CORT"),
  
  COST("COST"),
  
  CPKC("CPKC"),
  
  DCRD("DCRD"),
  
  DSMT("DSMT"),
  
  DVPM("DVPM"),
  
  EDUC("EDUC"),
  
  FACT("FACT"),
  
  FAND("FAND"),
  
  FCPM("FCPM"),
  
  FEES("FEES"),
  
  GOVT("GOVT"),
  
  ICCP("ICCP"),
  
  IDCP("IDCP"),
  
  IHRP("IHRP"),
  
  INSM("INSM"),
  
  IVPT("IVPT"),
  
  MCDM("MCDM"),
  
  MCFG("MCFG"),
  
  MSVC("MSVC"),
  
  NOWS("NOWS"),
  
  OCDM("OCDM"),
  
  OCFG("OCFG"),
  
  OFEE("OFEE"),
  
  OTHR("OTHR"),
  
  PADD("PADD"),
  
  PTSP("PTSP"),
  
  RCKE("RCKE"),
  
  RCPT("RCPT"),
  
  REBT("REBT"),
  
  REFU("REFU"),
  
  RENT("RENT"),
  
  REOD("REOD"),
  
  RIMB("RIMB"),
  
  RPNT("RPNT"),
  
  RRBN("RRBN"),
  
  RVPM("RVPM"),
  
  SLPI("SLPI"),
  
  SPLT("SPLT"),
  
  STDY("STDY"),
  
  TBAN("TBAN"),
  
  TBIL("TBIL"),
  
  TCSC("TCSC"),
  
  TELI("TELI"),
  
  TMPG("TMPG"),
  
  TPRI("TPRI"),
  
  TPRP("TPRP"),
  
  TRNC("TRNC"),
  
  TRVC("TRVC"),
  
  WEBI("WEBI"),
  
  ANNI("ANNI"),
  
  CAFI("CAFI"),
  
  CFDI("CFDI"),
  
  CMDT("CMDT"),
  
  DERI("DERI"),
  
  DIVD("DIVD"),
  
  FREX("FREX"),
  
  HEDG("HEDG"),
  
  INVS("INVS"),
  
  PRME("PRME"),
  
  SAVG("SAVG"),
  
  SECU("SECU"),
  
  SEPI("SEPI"),
  
  TREA("TREA"),
  
  UNIT("UNIT"),
  
  FNET("FNET"),
  
  FUTR("FUTR"),
  
  ANTS("ANTS"),
  
  CVCF("CVCF"),
  
  DMEQ("DMEQ"),
  
  DNTS("DNTS"),
  
  HLTC("HLTC"),
  
  HLTI("HLTI"),
  
  HSPC("HSPC"),
  
  ICRF("ICRF"),
  
  LTCF("LTCF"),
  
  MAFC("MAFC"),
  
  MARF("MARF"),
  
  MDCS("MDCS"),
  
  VIEW("VIEW"),
  
  CDEP("CDEP"),
  
  SWFP("SWFP"),
  
  SWPP("SWPP"),
  
  SWRS("SWRS"),
  
  SWUF("SWUF"),
  
  ADCS("ADCS"),
  
  AEMP("AEMP"),
  
  ALLW("ALLW"),
  
  ALMY("ALMY"),
  
  BBSC("BBSC"),
  
  BECH("BECH"),
  
  BENE("BENE"),
  
  BONU("BONU"),
  
  CCHD("CCHD"),
  
  COMM("COMM"),
  
  CSLP("CSLP"),
  
  GFRP("GFRP"),
  
  GVEA("GVEA"),
  
  GVEB("GVEB"),
  
  GVEC("GVEC"),
  
  GVED("GVED"),
  
  GWLT("GWLT"),
  
  HREC("HREC"),
  
  PAYR("PAYR"),
  
  PEFC("PEFC"),
  
  PENS("PENS"),
  
  PRCP("PRCP"),
  
  RHBS("RHBS"),
  
  SALA("SALA"),
  
  SSBE("SSBE"),
  
  LBIN("LBIN"),
  
  LCOL("LCOL"),
  
  LFEE("LFEE"),
  
  LMEQ("LMEQ"),
  
  LMFI("LMFI"),
  
  LMRK("LMRK"),
  
  LREB("LREB"),
  
  LREV("LREV"),
  
  LSFL("LSFL"),
  
  ESTX("ESTX"),
  
  FWLV("FWLV"),
  
  GSTX("GSTX"),
  
  HSTX("HSTX"),
  
  INTX("INTX"),
  
  NITX("NITX"),
  
  PTXP("PTXP"),
  
  RDTX("RDTX"),
  
  TAXS("TAXS"),
  
  VATX("VATX"),
  
  WHLD("WHLD"),
  
  TAXR("TAXR"),
  
  B112("B112"),
  
  BR12("BR12"),
  
  TLRF("TLRF"),
  
  TLRR("TLRR"),
  
  AIRB("AIRB"),
  
  BUSB("BUSB"),
  
  FERB("FERB"),
  
  RLWY("RLWY"),
  
  TRPT("TRPT"),
  
  CBTV("CBTV"),
  
  ELEC("ELEC"),
  
  ENRG("ENRG"),
  
  GASB("GASB"),
  
  NWCH("NWCH"),
  
  NWCM("NWCM"),
  
  OTLC("OTLC"),
  
  PHON("PHON"),
  
  UBIL("UBIL"),
  
  WTER("WTER");

  private String value;

  PurposeCode(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static PurposeCode fromValue(String text) {
    for (PurposeCode b : PurposeCode.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

