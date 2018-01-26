package com.lezhin.panther.lguplus;

import com.lezhin.panther.model.PGPayment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Jackson으로 변환시 uppercase를 보장하기 위해 getter를 명시적으로 선언.
 *
 * @author seoeun
 * @since 2017.12.16
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@ToString
public class LguplusPayment extends PGPayment {

    //public static final String DEFAULT_CST_MID =  "lezhin001";
    public static final String DEFAULT_WINDOW_TYPE = "iframe";
    public static final String LGD_MERTKEY = "f1232cf4cee3670e3bf6af125608275a"; // mall.conf 상점키

    private String CST_MID; // 상점 아이디
    private String CST_PLATFORM; // 플랫폼. test or service
    private String LGD_OID; // 주문번호. lezhin paymentId
    private String LGD_AMOUNT; // 결제금액
    private String LGD_BUYER; // 구매자 이름. lezhin userId
    private String LGD_PRODUCTINFO; // 구매 상품 이름 
    private String LGD_TIMESTAMP; // 20090226110637 (yyyyMMddHHmmss)
    private String LGD_CUSTOM_USABLEPAY;
    private String LGD_HASHDATA; //
    private String LGD_RETURNURL; // 인증결과 응답수신페이지 URL
    private String LGD_WINDOW_TYPE; // iframe or submit
    private String LGD_CUSTOM_SWITCHINGTYPE; //IFRAME or SUBMIT
    private String LGD_BUYERID; // 구매자아이디(상품권결제시 필수) 
    private String LGD_BUYERIP; // 구매자아이피(상품권결제시 필수) // TODO 필요 없음
    private String LGD_CUSTOM_PROCESSTYPE; // 상점정의 프로세스 타입   (기본값 : TWOTR)
    private String LGD_CASNOTEURL; // 무통장 입금 callbackUrl
    private String LGD_CLOSEDATE; // 무통장 입금 마감시간
    private String LGD_USABLECASBANK; // 무통장 사용가능 은행

    // optional
    private String LGD_BUYERADDRESS; // 구매자주소
    private String LGD_BUYERPHONE; // 구매자휴대폰번호
    private String LGD_BUYEREMAIL; //
    private String LGD_PRODUCTCODE; //
    private String LGD_OSTYPE_CHECK; // P or M
    private String LGD_ENCODING;
    private String LGD_ENCODING_RETURNURL;
    private String LGD_ENCODING_NOTEURL;
    private String LGD_VERSION;
    private String LGD_CUSTOM_SKIN;
    private String LGD_WINDOW_VER;

    // response
    private String LGD_RESPCODE; // 응답코드, '0000' 이면 성공 이외는 실패
    private String LGD_RESPMSG; // 응답메세지
    private String LGD_MID; // LG유플러스에서 부여한 상점ID
    private String LGD_PAYKEY; // LG유플러스 인증KEY

    // API request
    private String LGD_TXNAME; // "PaymentByKey"

    // final response
    private String LGD_TID;                // LG유플러스에서 부여한 거래번호
    private String LGD_PAYTYPE;            // 결제수단코드
    private String LGD_PAYDATE;            // 거래일시(승인일시/이체일시)
    private String LGD_FINANCECODE;        // 결제기관코드(은행코드)
    private String LGD_FINANCENAME;        // 결제기관이름(은행이름)
    private String LGD_ESCROWYN;           // 에스크로 적용여부
    private String LGD_PAYER;                // 임금자명
    // 무통장 ---
    private String LGD_ACCOUNTNUM;         // 계좌번호(무통장입금)
    private String LGD_CASTAMOUNT;         // 입금총액(무통장입금)
    private String LGD_CASCAMOUNT;         // 현입금액(무통장입금)
    private String LGD_CASFLAG;            // 무통장입금 플래그(무통장입금) - 'R':계좌할당, 'I':입금, 'C':입금취소
    private String LGD_CASSEQNO;           // 입금순서(무통장입금)
    private String LGD_CASHRECEIPTNUM;     // 현금영수증 승인번호
    private String LGD_CASHRECEIPTSELFYN;  // 현금영수증자진발급제유무 Y: 자진발급제 적용, 그외 : 미적용
    private String LGD_CASHRECEIPTKIND;    // 현금영수증 종류 0: 소득공제용 , 1: 지출증빙용
    private String LGD_CASHRECEIPTCODE;
    private String LGD_METHOD;             //ASSIGN:할당, CHANGE:변경
    // 구매정보
    private String LGD_BUYERSSN;           // 구매자 주민번호
    private String LGD_RECEIVER;           // 수취인
    private String LGD_RECEIVERPHONE;      // 수취인 전화번호
    private String LGD_DELIVERYINFO;       // 배송지
    // response. 문서에 없음
    private String LGD_IFOS;
    private String LGD_SAOWNER;
    private String LGD_2TR_FLAG;
    private String LGD_DEVICE;
    private String PARAMENCODING;
    private String PARAMTYPE;
    private String LGD_CASHASHDATA;
    private String LGD_PCANCELCNT;

    @JsonProperty("CST_MID")
    public String getCST_MID() {
        return CST_MID;
    }

    @JsonProperty("CST_PLATFORM")
    public String getCST_PLATFORM() {
        return CST_PLATFORM;
    }

    @JsonProperty("LGD_OID")
    public String getLGD_OID() {
        return LGD_OID;
    }

    @JsonProperty("LGD_AMOUNT")
    public String getLGD_AMOUNT() {
        return LGD_AMOUNT;
    }

    @JsonProperty("LGD_BUYER")
    public String getLGD_BUYER() {
        return LGD_BUYER;
    }

    @JsonProperty("LGD_PRODUCTINFO")
    public String getLGD_PRODUCTINFO() {
        return LGD_PRODUCTINFO;
    }

    @JsonProperty("LGD_TIMESTAMP")
    public String getLGD_TIMESTAMP() {
        return LGD_TIMESTAMP;
    }

    @JsonProperty("LGD_CUSTOM_USABLEPAY")
    public String getLGD_CUSTOM_USABLEPAY() {
        return LGD_CUSTOM_USABLEPAY;
    }

    @JsonProperty("LGD_HASHDATA")
    public String getLGD_HASHDATA() {
        return LGD_HASHDATA;
    }

    @JsonProperty("LGD_RETURNURL")
    public String getLGD_RETURNURL() {
        return LGD_RETURNURL;
    }

    @JsonProperty("LGD_WINDOW_TYPE")
    public String getLGD_WINDOW_TYPE() {
        return LGD_WINDOW_TYPE;
    }

    @JsonProperty("LGD_CUSTOM_SWITCHINGTYPE")
    public String getLGD_CUSTOM_SWITCHINGTYPE() {
        return LGD_CUSTOM_SWITCHINGTYPE;
    }

    @JsonProperty("LGD_BUYERID")
    public String getLGD_BUYERID() {
        return LGD_BUYERID;
    }

    @JsonProperty("LGD_BUYERIP")
    public String getLGD_BUYERIP() {
        return LGD_BUYERIP;
    }

    @JsonProperty("LGD_CUSTOM_PROCESSTYPE")
    public String getLGD_CUSTOM_PROCESSTYPE() {
        return LGD_CUSTOM_PROCESSTYPE;
    }

    @JsonProperty("LGD_CASNOTEURL")
    public String getLGD_CASNOTEURL() {
        return LGD_CASNOTEURL;
    }

    @JsonProperty("LGD_CLOSEDATE")
    public String getLGD_CLOSEDATE() {
        return LGD_CLOSEDATE;
    }

    public void setLGD_CLOSEDATE(String LGD_CLOSEDATE) {
        this.LGD_CLOSEDATE = LGD_CLOSEDATE;
    }

    @JsonProperty("LGD_USABLECASBANK")
    public String getLGD_USABLECASBANK() {
        return LGD_USABLECASBANK;
    }

    @JsonProperty("LGD_BUYERADDRESS")
    public String getLGD_BUYERADDRESS() {
        return LGD_BUYERADDRESS;
    }

    @JsonProperty("LGD_BUYERPHONE")
    public String getLGD_BUYERPHONE() {
        return LGD_BUYERPHONE;
    }

    @JsonProperty("LGD_BUYEREMAIL")
    public String getLGD_BUYEREMAIL() {
        return LGD_BUYEREMAIL;
    }

    @JsonProperty("LGD_PRODUCTCODE")
    public String getLGD_PRODUCTCODE() {
        return LGD_PRODUCTCODE;
    }

    @JsonProperty("LGD_OSTYPE_CHECK")
    public String getLGD_OSTYPE_CHECK() {
        return LGD_OSTYPE_CHECK;
    }

    @JsonProperty("LGD_RESPCODE")
    public String getLGD_RESPCODE() {
        return LGD_RESPCODE;
    }

    @JsonProperty("LGD_RESPMSG")
    public String getLGD_RESPMSG() {
        return LGD_RESPMSG;
    }

    @JsonProperty("LGD_MID")
    public String getLGD_MID() {
        return LGD_MID;
    }

    @JsonProperty("LGD_PAYKEY")
    public String getLGD_PAYKEY() {
        return LGD_PAYKEY;
    }

    @JsonProperty("LGD_TXNAME")
    public String getLGD_TXNAME() {
        return LGD_TXNAME;
    }

    @JsonProperty("LGD_TID")
    public String getLGD_TID() {
        return LGD_TID;
    }

    @JsonProperty("LGD_PAYTYPE")
    public String getLGD_PAYTYPE() {
        return LGD_PAYTYPE;
    }

    @JsonProperty("LGD_PAYDATE")
    public String getLGD_PAYDATE() {
        return LGD_PAYDATE;
    }

    @JsonProperty("LGD_FINANCECODE")
    public String getLGD_FINANCECODE() {
        return LGD_FINANCECODE;
    }

    @JsonProperty("LGD_FINANCENAME")
    public String getLGD_FINANCENAME() {
        return LGD_FINANCENAME;
    }

    @JsonProperty("LGD_ESCROWYN")
    public String getLGD_ESCROWYN() {
        return LGD_ESCROWYN;
    }

    @JsonProperty("LGD_ACCOUNTNUM")
    public String getLGD_ACCOUNTNUM() {
        return LGD_ACCOUNTNUM;
    }

    @JsonProperty("LGD_CASTAMOUNT")
    public String getLGD_CASTAMOUNT() {
        return LGD_CASTAMOUNT;
    }

    @JsonProperty("LGD_CASCAMOUNT")
    public String getLGD_CASCAMOUNT() {
        return LGD_CASCAMOUNT;
    }

    @JsonProperty("LGD_CASFLAG")
    public String getLGD_CASFLAG() {
        return LGD_CASFLAG;
    }

    @JsonProperty("LGD_CASSEQNO")
    public String getLGD_CASSEQNO() {
        return LGD_CASSEQNO;
    }

    @JsonProperty("LGD_CASHRECEIPTNUM")
    public String getLGD_CASHRECEIPTNUM() {
        return LGD_CASHRECEIPTNUM;
    }

    @JsonProperty("LGD_CASHRECEIPTSELFYN")
    public String getLGD_CASHRECEIPTSELFYN() {
        return LGD_CASHRECEIPTSELFYN;
    }

    @JsonProperty("LGD_CASHRECEIPTKIND")
    public String getLGD_CASHRECEIPTKIND() {
        return LGD_CASHRECEIPTKIND;
    }

    @JsonProperty("LGD_CASHRECEIPTCODE")
    public String getLGD_CASHRECEIPTCODE() {
        return LGD_CASHRECEIPTCODE;
    }

    @JsonProperty("LGD_METHOD")
    public String getLGD_METHOD() {
        return LGD_METHOD;
    }

    @JsonProperty("LGD_PAYER")
    public String getLGD_PAYER() {
        return LGD_PAYER;
    }

    @JsonProperty("LGD_BUYERSSN")
    public String getLGD_BUYERSSN() {
        return LGD_BUYERSSN;
    }

    @JsonProperty("LGD_RECEIVER")
    public String getLGD_RECEIVER() {
        return LGD_RECEIVER;
    }

    @JsonProperty("LGD_RECEIVERPHONE")
    public String getLGD_RECEIVERPHONE() {
        return LGD_RECEIVERPHONE;
    }

    @JsonProperty("LGD_DELIVERYINFO")
    public String getLGD_DELIVERYINFO() {
        return LGD_DELIVERYINFO;
    }

    @JsonProperty("LGD_IFOS")
    public String getLGD_IFOS() {
        return LGD_IFOS;
    }

    @JsonProperty("LGD_SAOWNER")
    public String getLGD_SAOWNER() {
        return LGD_SAOWNER;
    }

    @JsonProperty("LGD_2TR_FLAG")
    public String getLGD_2TR_FLAG() {
        return LGD_2TR_FLAG;
    }

    @JsonProperty("LGD_DEVICE")
    public String getLGD_DEVICE() {
        return LGD_DEVICE;
    }

    @JsonProperty("PARAMENCODING")
    public String getPARAMENCODING() {
        return PARAMENCODING;
    }

    @JsonProperty("PARAMTYPE")
    public String getPARAMTYPE() {
        return  PARAMTYPE;
    }
    
    @JsonProperty("LGD_CASHASHDATA")
    public String getLGD_CASHASHDATA() {
        return LGD_CASHASHDATA;
    }

    @JsonProperty("LGD_PCANCELCNT")
    public String getLGD_PCANCELCNT() {
        return LGD_PCANCELCNT;
    }

    public String getApprovalId() {
        return this.LGD_TID;
    }

    public Map<String, Object> createReceipt() {
        Map<String, Object> receipt = new HashMap<>();
        receipt.put("CST_PLATFORM", CST_PLATFORM);
        receipt.put("CST_MID", CST_MID); // 상점아이디
        receipt.put("LGD_TID", LGD_TID);  // TransactionId
        receipt.put("LGD_OID", LGD_OID);  // TransactionId
        receipt.put("LGD_CLOSEDATE", LGD_CLOSEDATE); // 가상계좌 close
        return receipt;
    }

    public enum STORETYPE {

        web("iframe", "IFRAME", "P"),
        mobile("submit", "SUBMIT", "M");

        private String LGD_WINDOW_TYPE;
        private String LGD_CUSTOM_SWITCHINGTYPE;
        private String LGD_OSTYPE_CHECK;

        STORETYPE(String LGD_WINDOW_TYPE, String LGD_CUSTOM_SWITCHINGTYPE, String LGD_OSTYPE_CHECK) {
            this.LGD_WINDOW_TYPE = LGD_WINDOW_TYPE;
            this.LGD_CUSTOM_SWITCHINGTYPE = LGD_CUSTOM_SWITCHINGTYPE;
            this.LGD_OSTYPE_CHECK = LGD_OSTYPE_CHECK;
        }

        public static STORETYPE getAPI(boolean isMobile) {
            if (!isMobile) {
                return web;
            } else {
                return mobile;
            }
        }

        public String getLGD_WINDOW_TYPE() {
            return LGD_WINDOW_TYPE;
        }

        public String getLGD_CUSTOM_SWITCHINGTYPE() {
            return LGD_CUSTOM_SWITCHINGTYPE;
        }

        public String getLGD_OSTYPE_CHECK() {
            return LGD_OSTYPE_CHECK;
        }
    }

}
