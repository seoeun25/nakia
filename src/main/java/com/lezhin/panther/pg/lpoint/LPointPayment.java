package com.lezhin.panther.pg.lpoint;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lezhin.panther.model.PGPayment;
import com.lezhin.panther.util.DateUtil;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author taeyoung
 * @since 2018.05.25
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LPointPayment extends PGPayment {
    public static final String INST_CD = "O730";



    private LPointControl control;
    private String copMcno;         // 제휴가맹점번호(10)

    /**
     * O120 개방형 회원인증_CI 요청
     */
    // 요청
    private String wcc;             // WCC
    private String aprAkMdDc;       // 승인요청방식구분코드 - "6": CI
    private String ciNo;            // CI 번호(88)
    // 응답
    private String ctfCno;          // 고객번호(10)
    private Integer avlPt;          // 가용포인트
    private Integer gftPt;          // 기프트포인트
    private String rspSgnC;         // 잔여포인트부호코드
    private Integer resPt;          // 잔여포인트
    private String ptRcPsyn;        // 포인트적립가능여부 {"1" 포인트적립가능, "0": 포인트적립불가}
    private String ptUPsyn;         // 포인트사용가능여부 {"1" 포인트사용가능, "0": 포인트사용불가}
    private String ptAdpAplYn;      // 포인트합산 신청여부

    /**
     * O420 개방형 전환가능포인트 조회 요청
     */
    // 요청
    private String cstDrmDc;        // 고객식별구분코드(1) - "2": 고객번호
    private String cstDrmV;         // 고객식별값(20) - 고객번호 10자리
    // 응답
    private Integer cvAvlPt;        // 전환가능포인트

    /**
     * O440 개방형 포인트전환 신청 & O410 개방형 포인트전환 취소 요청
     */
    // 요청 공통
    private String ccoAprno;        // 제휴사승인번호(19)
    private String deDt;            // 거래일자(8) - YYYYMMDD
    private String deHr;            // 거래시간(6) - HHMMSS
    private String tfmviDc;         // 이수관구분코드(1) {"1": 이관, "2": 수관, "3": 이관재적립}
    private String deDc;            // 거래구분코드(2) {"20": 포인트사용, "30": 포인트조정}
    private String deRsc;           // 거래사유코드(3) {"230": 포인트전환(이관), "330": 포인트전환(수관)}
    private String rvUDc;           // 적립사용구분코드(1) {"1": 정상}
    private Integer akCvPt;         // 요청전환포인트(9) - 전환요청 포인트, 이관시 필수
    private Integer ccoCvAm;        // 제휴사전환금액(13) - 제휴사전환금액, 수관시 필수
    // O440 요청
    private String pswd;            // 비밀번호 - MD5로 암호화된 값
    private String ccoCstDrmDc;     // 제휴사고객식별구분코드(1) {"1": 카드번호, "2": 휴대폰, "3": ATM, "4":제휴사회원번호, "5": CI 번호, "6": 상품권}
    private String ccoCstDrmV;      // 제휴사고객식별값(100)
    // O410 요청
    private String otlnfDc;         // 원거래정보구분코드 {"1": 운영사 원승인정보, "2": 제휴사 원승인정보}
    private String otAprno;         // 원거래승인번호
    private String otDt;            // 원거래일자

    // 응답 공통
    private String aprno;           // 승인번호
    private String aprDt;           // 승인일자
    private String aprHr;           // 승인시간
    // O440 응답
    private Integer ttnCvPt;        // 금회전환포인트
    // O410 응답
    private Integer canPt;          // 취소포인트

    /**
     * O720 온라인 비밀번호 인증 요청
     */
    // 요청
    private String cno;             // 고객번호(11) - 멤버스고객번호 10자리
    //    private String pswd;            // 비밀번호
    // 응답
    private String rspC;            // 응답코드 {"0": 성공, "1": 비밀번호오류, "2":비밀번호등록고객아님, "3":비밀번호오류횟수초과, "4":해당고객없음, "5": 카드번호오류, "6"; 기타오류}
    private String rspMsgCn;        // 응답 메시지내용
    private Integer pswdErrTms;     // 비밀번호오류횟수

    // 공통 not used
    private String msg;             // not used (POS 단말용)
    private String msgCn1;          // not used (POS 단말용)
    private String msgCn2;          // not used (POS 단말용)
    private String filter;          // not used (POS 단말용)

    public LPointPayment() {
    }

    public LPointPayment(String flwNo) {
        this.control = new LPointControl(flwNo, RequestCodeType.DEFAULT.code);
    }

    class LPointControl {
        private String flwNo;   // 추적번호
        private String rspC;    // 응답코드 {요청: " ", 정상: "00", 망취소: "60"}

        public LPointControl() {
        }

        public LPointControl(String flwNo, String rspC) {
            this.flwNo = flwNo;
            this.rspC = rspC != null? rspC : " ";
        }

        public String getFlwNo() {
            return flwNo;
        }

        public String getRspC() {
            return rspC;
        }

        public void setRspC(String rspC) {
            this.rspC = rspC;
        }

        @Override
        public String toString() {
            return "flwNo='" + flwNo + '\'' +
                    ", rspC='" + rspC + '\'';
        }
    }

    public enum API {
        HEALTHCHECK("9900", "통신망관리"),
        AUTHENTICATION("O120", "회원인증(CI)"),
        POINT_CANCEL("O410", "포인트 전환취소"),
        POINT_CHECK("O420", "전환가능 포인트조회"),
        POINT_USE("O440", "포인트 전환요청"),
        PSWD_CHECK("O720", "온라인 비밀번호 인증요청");

        private String id;
        private String name;

        API(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public LPointPayment createReqeust() {
            // 추적번호 생성: 서비스ID(전문ID) + 기관코드 + 요청일(yyyyMMdd) + 일련번호(6자리)
            Instant now = Instant.now();
            String deDt = DateUtil.getDateString(now.toEpochMilli());
            String deHr = DateUtil.getTimeString(now.toEpochMilli());
            String flwNo = String.format("%s%s%s%s", id, LPointPayment.INST_CD, deDt, deHr);

            LPointPayment payment = new LPointPayment(flwNo);
            payment.setDeDt(deDt);
            payment.setDeHr(deHr);

            return payment;
        }
    }

    public enum RequestCodeType {
        DEFAULT(" ", "기본"),
        RE_REQUEST("50", "재전송 요청(적립대행)"),   // 사용안함
        CANCEL_REQUEST("60", "망취소 요청");

        private String code;
        private String name;

        RequestCodeType(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    @Override
    public Map<String, Object> createReceipt() {
        Map<String, Object> receipt = new HashMap<>();
        receipt.put("flwNo", control.getFlwNo());
        receipt.put("rspC", control.getRspC());
        receipt.put("msg", msg);
        receipt.put("cstDrmV", cstDrmV);    // 고객식별값
        receipt.put("aprno", aprno);        // 승인번호
        receipt.put("aprDt", aprDt);        // 승인일자
        receipt.put("aprHr", aprHr);        // 승인시간
        receipt.put("ttnCvPt", ttnCvPt);    // 금회전환포인트
        receipt.put("avlPt", avlPt);        // 가용포인트
        receipt.put("cvAvlPt", cvAvlPt);    // 전환가능포인트

        return receipt;
    }

    public LPointControl getControl() {
        return control;
    }

    public void setControl(LPointControl control) {
        this.control = control;
    }

    public String getCopMcno() {
        return copMcno;
    }

    public void setCopMcno(String copMcno) {
        this.copMcno = copMcno;
    }

    public String getWcc() {
        return wcc;
    }

    public void setWcc(String wcc) {
        this.wcc = wcc;
    }

    public String getAprAkMdDc() {
        return aprAkMdDc;
    }

    public void setAprAkMdDc(String aprAkMdDc) {
        this.aprAkMdDc = aprAkMdDc;
    }

    public String getCiNo() {
        return ciNo;
    }

    public void setCiNo(String ciNo) {
        this.ciNo = ciNo;
    }

    public String getCtfCno() {
        return ctfCno;
    }

    public void setCtfCno(String ctfCno) {
        this.ctfCno = ctfCno;
    }

    public Integer getAvlPt() {
        return avlPt;
    }

    public void setAvlPt(Integer avlPt) {
        this.avlPt = avlPt;
    }

    public Integer getGftPt() {
        return gftPt;
    }

    public void setGftPt(Integer gftPt) {
        this.gftPt = gftPt;
    }

    public String getRspSgnC() {
        return rspSgnC;
    }

    public void setRspSgnC(String rspSgnC) {
        this.rspSgnC = rspSgnC;
    }

    public Integer getResPt() {
        return resPt;
    }

    public void setResPt(Integer resPt) {
        this.resPt = resPt;
    }

    public String getPtRcPsyn() {
        return ptRcPsyn;
    }

    public void setPtRcPsyn(String ptRcPsyn) {
        this.ptRcPsyn = ptRcPsyn;
    }

    public String getPtUPsyn() {
        return ptUPsyn;
    }

    public void setPtUPsyn(String ptUPsyn) {
        this.ptUPsyn = ptUPsyn;
    }

    public String getPtAdpAplYn() {
        return ptAdpAplYn;
    }

    public void setPtAdpAplYn(String ptAdpAplYn) {
        this.ptAdpAplYn = ptAdpAplYn;
    }

    public String getCstDrmDc() {
        return cstDrmDc;
    }

    public void setCstDrmDc(String cstDrmDc) {
        this.cstDrmDc = cstDrmDc;
    }

    public String getCstDrmV() {
        return cstDrmV;
    }

    public void setCstDrmV(String cstDrmV) {
        this.cstDrmV = cstDrmV;
    }

    public Integer getCvAvlPt() {
        return cvAvlPt;
    }

    public void setCvAvlPt(Integer cvAvlPt) {
        this.cvAvlPt = cvAvlPt;
    }

    public String getCcoAprno() {
        return ccoAprno;
    }

    public void setCcoAprno(String ccoAprno) {
        this.ccoAprno = ccoAprno;
    }

    public String getDeDt() {
        return deDt;
    }

    public void setDeDt(String deDt) {
        this.deDt = deDt;
    }

    public String getDeHr() {
        return deHr;
    }

    public void setDeHr(String deHr) {
        this.deHr = deHr;
    }

    public String getTfmviDc() {
        return tfmviDc;
    }

    public void setTfmviDc(String tfmviDc) {
        this.tfmviDc = tfmviDc;
    }

    public String getDeDc() {
        return deDc;
    }

    public void setDeDc(String deDc) {
        this.deDc = deDc;
    }

    public String getDeRsc() {
        return deRsc;
    }

    public void setDeRsc(String deRsc) {
        this.deRsc = deRsc;
    }

    public String getRvUDc() {
        return rvUDc;
    }

    public void setRvUDc(String rvUDc) {
        this.rvUDc = rvUDc;
    }

    public Integer getAkCvPt() {
        return akCvPt;
    }

    public void setAkCvPt(Integer akCvPt) {
        this.akCvPt = akCvPt;
    }

    public Integer getCcoCvAm() {
        return ccoCvAm;
    }

    public void setCcoCvAm(Integer ccoCvAm) {
        this.ccoCvAm = ccoCvAm;
    }

    public String getPswd() {
        return pswd;
    }

    public void setPswd(String pswd) {
        this.pswd = pswd;
    }

    public String getCcoCstDrmDc() {
        return ccoCstDrmDc;
    }

    public void setCcoCstDrmDc(String ccoCstDrmDc) {
        this.ccoCstDrmDc = ccoCstDrmDc;
    }

    public String getCcoCstDrmV() {
        return ccoCstDrmV;
    }

    public void setCcoCstDrmV(String ccoCstDrmV) {
        this.ccoCstDrmV = ccoCstDrmV;
    }

    public String getOtlnfDc() {
        return otlnfDc;
    }

    public void setOtlnfDc(String otlnfDc) {
        this.otlnfDc = otlnfDc;
    }

    public String getOtAprno() {
        return otAprno;
    }

    public void setOtAprno(String otAprno) {
        this.otAprno = otAprno;
    }

    public String getOtDt() {
        return otDt;
    }

    public void setOtDt(String otDt) {
        this.otDt = otDt;
    }

    public String getAprno() {
        return aprno;
    }

    public void setAprno(String aprno) {
        this.aprno = aprno;
    }

    public String getAprDt() {
        return aprDt;
    }

    public void setAprDt(String aprDt) {
        this.aprDt = aprDt;
    }

    public String getAprHr() {
        return aprHr;
    }

    public void setAprHr(String aprHr) {
        this.aprHr = aprHr;
    }

    public Integer getTtnCvPt() {
        return ttnCvPt;
    }

    public void setTtnCvPt(Integer ttnCvPt) {
        this.ttnCvPt = ttnCvPt;
    }

    public Integer getCanPt() {
        return canPt;
    }

    public void setCanPt(Integer canPt) {
        this.canPt = canPt;
    }

    public String getCno() {
        return cno;
    }

    public void setCno(String cno) {
        this.cno = cno;
    }

    public String getRspC() {
        return rspC;
    }

    public void setRspC(String rspC) {
        this.rspC = rspC;
    }

    public String getRspMsgCn() {
        return rspMsgCn;
    }

    public void setRspMsgCn(String rspMsgCn) {
        this.rspMsgCn = rspMsgCn;
    }

    public Integer getPswdErrTms() {
        return pswdErrTms;
    }

    public void setPswdErrTms(Integer pswdErrTms) {
        this.pswdErrTms = pswdErrTms;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsgCn1() {
        return msgCn1;
    }

    public void setMsgCn1(String msgCn1) {
        this.msgCn1 = msgCn1;
    }

    public String getMsgCn2() {
        return msgCn2;
    }

    public void setMsgCn2(String msgCn2) {
        this.msgCn2 = msgCn2;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
