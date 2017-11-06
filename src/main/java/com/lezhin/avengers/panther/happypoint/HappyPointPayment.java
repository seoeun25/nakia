package com.lezhin.avengers.panther.happypoint;


import com.lezhin.avengers.panther.model.PGPayment;
import com.lezhin.avengers.panther.util.DateUtil;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;

/**
 * @author seoeun
 * @since 2017.10.25
 */
@JsonSerialize
public class HappyPointPayment extends PGPayment {

    /**
     * 제휴사 식별키
     */
    private String rcgnKey = "dd50084c773c417f0b75bf1be857e5ddf9582f7ab8480b38e282938a757bbfe7";

    // 공통
    private String instCd = "ORJN"; // 기관코드
    private String tlgmNo; // 전문번호 "2000" 회원인증, "5120" 포인트사용(취소)
    private String tlgmChnlCd = "X0"; //전문채널코드
    private String trsDt; // 전송일자 YYYYMMDD 송신 측 전송일자
    private String trsTm; // 전송시각 HH24MISS 송신 측 전송시각
    private String tracNo; // 추적번호 동일 거래에 대해 {기관코드 + 전송일자 + 추적번호}는 Unique 해야 함
    private String tlgmClCd = "ON"; // 전문구분코드 ON" 온라인, "OB" 온라인배치
    private String rpsCd; // 응답코드 "00" 응답(정상), "nn" 응답(거절)
    private String rpsMsgCtt; // 응답메시지내용
    // 공통2 request
    private String reqClCd = "10"; // 요청구분코드. 10:회원인증, 10:포인트조회, 10:포인트사용
    private String reqBrndCd = "C083"; // 요청브랜드코드
    private String reqChnlCd = "20";  // 요청채널코드. 20=홈페이지
    // 공통2 response
    private String rpsDtlCd; // 응답상세코드
    private String rpsDtlMsg; // 응답상세메시지
    // 회원인증 request
    private String mbrAuthMthdCd = "20"; // 회원인증방법코드 20=회원명 + 회원식별번호
    private String mbrNm; // 회원명
    private String mbrIdfNo; // 회원식별번호
    // 회원인증 응답
    private String mbrNo; // 회원번호
    private String cardStCd; //카드상태코드
    private String mbrGrCd; // 회원등급코드
    private String mbrGrCdNm; // 회원등급코드명
    private String remPt; // 잔여포인트
    // 포인트 조회
    private String mbrIdfWayCd = "20"; // 회원식별방식코드 10:카드번호, 20:회원번호
    //private String mbrNo; // 회원번호
    private String mchtNo = "100015851"; // 가맹점번호 개발환경 : "100015851"
    private String ptResvPossYn; // 포인트사용가능여부
    private String ptUsePossYn; // 포인트사용가능여부
    private String ptUsePwdAplyYn; // 포인트사용비밀번호적용
    private String ptConvPossYn; // 포인트전환가능여부
    //private String remPt; // 잔여포인트
    private String usblPt; // 가용포인트
    // 포인트 사용/취소
    //private String mbrIdfWayCd = "20"; // 회원식별방식코드
    //private String mbrNo; // 회원번호
    //private String mchtNo ="100015851"; // 가맹점번호
    private String trxDt; // 거래일자
    private String trxTm; // 거래시각
    private String trxClCd; // 거래구분코드 20:포인트사용
    private String trxTypCd; // 거래유형코드 10:사용, 20:사용취소
    private String trxRsnCd = "2001"; // 거래사유코드 2001:대금결제
    private String trxAmt; // 거래금액
    //-------
    private String useReqPt; // 사용요청포인트
    private String aprvDt; // 승인일자
    private String aprvNo; // 승인번호
    private String usePt; // 사용포인트
    public HappyPointPayment() {

    }
    //private String remPt; // 잔여포인트

    public String getRcgnKey() {
        return rcgnKey;
    }

    public void setRcgnKey(String rcgnKey) {
        this.rcgnKey = rcgnKey;
    }

    public String getInstCd() {
        return instCd;
    }

    public void setInstCd(String instCd) {
        this.instCd = instCd;
    }

    public String getTlgmNo() {
        return tlgmNo;
    }

    public void setTlgmNo(String tlgmNo) {
        this.tlgmNo = tlgmNo;
    }

    public String getTlgmChnlCd() {
        return tlgmChnlCd;
    }

    public void setTlgmChnlCd(String tlgmChnlCd) {
        this.tlgmChnlCd = tlgmChnlCd;
    }

    public String getTrsDt() {
        return trsDt;
    }

    public void setTrsDt(String trsDt) {
        this.trsDt = trsDt;
    }

    public String getTrsTm() {
        return trsTm;
    }

    public void setTrsTm(String trsTm) {
        this.trsTm = trsTm;
    }

    public String getTracNo() {
        return tracNo;
    }

    public void setTracNo(String tracNo) {
        this.tracNo = tracNo;
    }

    public String getTlgmClCd() {
        return tlgmClCd;
    }

    public void setTlgmClCd(String tlgmClCd) {
        this.tlgmClCd = tlgmClCd;
    }

    public String getRpsCd() {
        return rpsCd;
    }

    public void setRpsCd(String rpsCd) {
        this.rpsCd = rpsCd;
    }

    public String getRpsMsgCtt() {
        return rpsMsgCtt;
    }

    public void setRpsMsgCtt(String rpsMsgCtt) {
        this.rpsMsgCtt = rpsMsgCtt;
    }

    public String getReqClCd() {
        return reqClCd;
    }

    public void setReqClCd(String reqClCd) {
        this.reqClCd = reqClCd;
    }

    public String getReqBrndCd() {
        return reqBrndCd;
    }

    public void setReqBrndCd(String reqBrndCd) {
        this.reqBrndCd = reqBrndCd;
    }

    public String getReqChnlCd() {
        return reqChnlCd;
    }

    public void setReqChnlCd(String reqChnlCd) {
        this.reqChnlCd = reqChnlCd;
    }

    public String getRpsDtlCd() {
        return rpsDtlCd;
    }

    public void setRpsDtlCd(String rpsDtlCd) {
        this.rpsDtlCd = rpsDtlCd;
    }

    public String getRpsDtlMsg() {
        return rpsDtlMsg;
    }

    public void setRpsDtlMsg(String rpsDtlMsg) {
        this.rpsDtlMsg = rpsDtlMsg;
    }

    public String getMbrAuthMthdCd() {
        return mbrAuthMthdCd;
    }

    public void setMbrAuthMthdCd(String mbrAuthMthdCd) {
        this.mbrAuthMthdCd = mbrAuthMthdCd;
    }

    public String getMbrNm() {
        return mbrNm;
    }

    public void setMbrNm(String mbrNm) {
        this.mbrNm = mbrNm;
    }

    public String getMbrIdfNo() {
        return mbrIdfNo;
    }

    public void setMbrIdfNo(String mbrIdfNo) {
        this.mbrIdfNo = mbrIdfNo;
    }

    public String getMbrNo() {
        return mbrNo;
    }

    public void setMbrNo(String mbrNo) {
        this.mbrNo = mbrNo;
    }

    public String getCardStCd() {
        return cardStCd;
    }

    public void setCardStCd(String cardStCd) {
        this.cardStCd = cardStCd;
    }

    public String getMbrGrCd() {
        return mbrGrCd;
    }

    public void setMbrGrCd(String mbrGrCd) {
        this.mbrGrCd = mbrGrCd;
    }

    public String getMbrGrCdNm() {
        return mbrGrCdNm;
    }

    public void setMbrGrCdNm(String mbrGrCdNm) {
        this.mbrGrCdNm = mbrGrCdNm;
    }

    public String getRemPt() {
        return remPt;
    }

    public void setRemPt(String remPt) {
        this.remPt = remPt;
    }

    public String getMbrIdfWayCd() {
        return mbrIdfWayCd;
    }

    public void setMbrIdfWayCd(String mbrIdfWayCd) {
        this.mbrIdfWayCd = mbrIdfWayCd;
    }

    public String getMchtNo() {
        return mchtNo;
    }

    public void setMchtNo(String mchtNo) {
        this.mchtNo = mchtNo;
    }

    public String getPtResvPossYn() {
        return ptResvPossYn;
    }

    public void setPtResvPossYn(String ptResvPossYn) {
        this.ptResvPossYn = ptResvPossYn;
    }

    public String getPtUsePossYn() {
        return ptUsePossYn;
    }

    public void setPtUsePossYn(String ptUsePossYn) {
        this.ptUsePossYn = ptUsePossYn;
    }

    public String getPtUsePwdAplyYn() {
        return ptUsePwdAplyYn;
    }

    public void setPtUsePwdAplyYn(String ptUsePwdAplyYn) {
        this.ptUsePwdAplyYn = ptUsePwdAplyYn;
    }

    public String getPtConvPossYn() {
        return ptConvPossYn;
    }

    public void setPtConvPossYn(String ptConvPossYn) {
        this.ptConvPossYn = ptConvPossYn;
    }

    public String getUsblPt() {
        return usblPt;
    }

    public void setUsblPt(String usblPt) {
        this.usblPt = usblPt;
    }

    public String getTrxDt() {
        return trxDt;
    }

    public void setTrxDt(String trxDt) {
        this.trxDt = trxDt;
    }

    public String getTrxTm() {
        return trxTm;
    }

    public void setTrxTm(String trxTm) {
        this.trxTm = trxTm;
    }

    public String getTrxClCd() {
        return trxClCd;
    }

    public void setTrxClCd(String trxClCd) {
        this.trxClCd = trxClCd;
    }

    public String getTrxTypCd() {
        return trxTypCd;
    }

    public void setTrxTypCd(String trxTypCd) {
        this.trxTypCd = trxTypCd;
    }

    public String getTrxRsnCd() {
        return trxRsnCd;
    }

    public void setTrxRsnCd(String trxRsnCd) {
        this.trxRsnCd = trxRsnCd;
    }

    public String getTrxAmt() {
        return trxAmt;
    }

    public void setTrxAmt(String trxAmt) {
        this.trxAmt = trxAmt;
    }

    public String getUseReqPt() {
        return useReqPt;
    }

    public void setUseReqPt(String useReqPt) {
        this.useReqPt = useReqPt;
    }

    public String getAprvDt() {
        return aprvDt;
    }

    public void setAprvDt(String aprvDt) {
        this.aprvDt = aprvDt;
    }

    public String getAprvNo() {
        return aprvNo;
    }

    public void setAprvNo(String aprvNo) {
        this.aprvNo = aprvNo;
    }

    public String getUsePt() {
        return usePt;
    }

    public void setUsePt(String usePt) {
        this.usePt = usePt;
    }

    public String printCommonRequest() {
        return MoreObjects.toStringHelper(this)
                .add("rcgnKey", rcgnKey)
                .add("instCd", instCd)
                .add("tlgmNo", tlgmNo)
                .add("tlgmChnlCd", tlgmChnlCd)
                .add("trsDt", trsDt)
                .add("trsTm", trsTm)
                .add("tracNo", tracNo)
                .add("tlgmClCd", tlgmClCd)
                .add("reqClCd", tracNo)
                .add("reqBrndCd", reqBrndCd)
                .add("reqChnlCd", reqChnlCd)
                .toString();
    }

    public String printA() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues().toString();

    }

    public enum API {
        // FIXME 포인트 조회에 대한 전문 번호 없음.
        // FIXME 요청구분코드 가 모든 api 에 동일. (전문번호가 틀려서 요청구분코드가 같아도 상관없음)
        authentication("2000", "10"), // 회원인증
        pointcheck("5000", "10"), // 포인트조회
        pointuse("5120", "10"), // 포인트사용
        pointrefund("5120", "10"); // 포인트취소
        private String tlgmNo;
        private String reqClCd;

        API(String tlgmNo, String reqClCd) {
            this.tlgmNo = tlgmNo;
            this.reqClCd = reqClCd;
        }

        public String getTlgmNo() {
            return tlgmNo;
        }

        public String getReqClCd() {
            return reqClCd;
        }

        public HappyPointPayment createRequest() {
            HappyPointPayment happyPointPayment = new HappyPointPayment();
            happyPointPayment.setTlgmNo(tlgmNo);
            long current = System.currentTimeMillis();
            happyPointPayment.setTrsDt(DateUtil.getDateString(current));
            happyPointPayment.setTrsTm(DateUtil.getTimeString(current));
            happyPointPayment.setReqClCd(reqClCd);
            return happyPointPayment;
        }
    }
}
