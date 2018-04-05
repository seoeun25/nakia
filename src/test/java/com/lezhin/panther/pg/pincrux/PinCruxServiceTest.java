package com.lezhin.panther.pg.pincrux;

import com.lezhin.panther.config.PantherProperties;
import com.lezhin.panther.util.JsonUtil;

import com.google.api.client.util.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author seoeun
 * @since 2018.03.20
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = {"test"})
public class PinCruxServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(PinCruxServiceTest.class);

    @Autowired
    private PantherProperties pantherProperties;

    @Autowired
    private ADEventRepository adEventRepository;

    private PinCruxService pinCruxService;

    @BeforeEach
    public void setUp() {
        pinCruxService = new PinCruxService(pantherProperties, null, null, null,
                adEventRepository);
    }

    /**
     * Pincrux.offer 로 받은 json을 CruxADs 로 변환.
     *
     * @throws IOException
     */
    @Test
    public void testJsonParse() throws IOException {

        Resource resource = new ClassPathResource("/example/pincrux/v1_data_org_production.json");
        assertNotNull(resource.getInputStream());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(resource.getInputStream(), outputStream);
        String abc = outputStream.toString();

        CruxADs ads = JsonUtil.fromJson(abc, CruxADs.class);
        String converted = JsonUtil.toJson(ads);
        logger.info("data converted = \n{}\n", JsonUtil.toJson(ads));
        assertEquals(true, converted.contains("itemCount"));

        List<Item> items = ads.getItems();
        assertEquals(2, items.size());
        Item item1 = items.get(0);
        assertEquals(102415, item1.getAppkey().intValue());
        assertEquals("1코인", item1.getCoin());
        assertEquals(150, item1.getFee().intValue());
        assertNull(item1.getCoinInt());
        //assertEquals(PinCruxService.getFee2Coin(item1.getFee().doubleValue()), item1.getCoinInt());
        logger.info("item1 = \n{}\n", JsonUtil.toJson(item1));

        Item item2 = items.get(1);
        assertEquals(102764, item2.getAppkey().intValue());
        assertEquals("2코인", item2.getCoin());
        assertEquals(170, item2.getFee().intValue());
        //assertEquals(2, item2.getCoinInt().intValue()); // TODO
        //assertEquals(PinCruxService.getFee2Coin(item2.getFee().doubleValue()), item2.getCoinInt());
        logger.info("item2 = \n{}\n", JsonUtil.toJson(item2));

    }

    /**
     * Pincrux.offer 로 받은 json을 CruxADs 로 변환. From Pincrux test server.
     *
     * @throws IOException
     */
    @Test
    public void testJsonParse2() throws IOException {

        Resource resource = new ClassPathResource("/example/pincrux/v2_data_pincrux_test.json");
        assertNotNull(resource.getInputStream());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IOUtils.copy(resource.getInputStream(), outputStream);
        String abc = outputStream.toString();

        CruxADs ads = JsonUtil.fromJson(abc, CruxADs.class);
        String converted = JsonUtil.toJson(ads);
        logger.info("data converted = \n{}\n", JsonUtil.toJson(ads));
        assertEquals(true, converted.contains("itemCount"));
        assertNotNull(ads.getDisplayAd());
        assertEquals("http://test-lezhin.pincrux.com/lezhin_img/1519027906.png", ads.getDisplayAd().getImage());
        assertEquals("lezhin://freecoins", ads.getDisplayAd().getUrl());

        List<Item> items = ads.getItems();
        assertEquals(2, items.size());
        Item item1 = items.get(0);
        assertEquals(102829, item1.getAppkey().intValue());
        assertEquals("32코인", item1.getCoin());
        assertEquals(5000, item1.getFee().intValue());
        assertEquals(32, item1.getCoinInt().intValue());
        assertEquals("설치형 테스트입니다.두번째줄입니다.", item1.getAppName());
        assertEquals("앱 설치하면 되요", item1.getActionPlan());
        assertEquals(0, item1.getOsFlag().intValue());
        assertNotNull(item1.getListImg());
        assertEquals(720, item1.getListImg().getWidth().intValue());
        assertEquals("앱설치하면 코인지급합니다.오호호호", item1.getViewTitle());
        assertEquals("이런식으로 하시면 됩니다.헤헤", item1.getViewSubTitle());
        assertEquals("코인지급하기", item1.getViewButton());
        assertNotNull(item1.getViewTitleImg());
        Item.CruxImage viewTitleImage = item1.getViewTitleImg();
        assertEquals(720, viewTitleImage.getWidth().intValue());
        assertEquals(780, viewTitleImage.getHeight().intValue());
        assertNotNull(item1.getContext());

        Item item2 = items.get(1);
        assertEquals(102770, item2.getAppkey().intValue());
        assertEquals("1코인", item2.getCoin());
        assertEquals(160, item2.getFee().intValue());
        assertEquals(1, item2.getCoinInt().intValue());
        assertEquals("tv.filecity.webhard.filecitystreaming4", item2.getPackageName());
        logger.info("item2 = \n{}\n", JsonUtil.toJson(item2));

    }

    @Test
    public void testUrlComposer() throws Exception {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("http").host("api.pincrux.com")
                .path("/offer.pin")
                .queryParam("pubkey", "pubkey-aaa")
                .queryParam("usrkey", "-1")
                .queryParam("os_flag", "0")
                .build()
                .encode();
        logger.info("url 1 = {}", uriComponents.toUriString());

        UriComponents uriComponents2 = UriComponentsBuilder.newInstance()
                .uri(new URI("http://api.pincrux.com"))
                .path("/offer.pin")
                .queryParam("pubkey", "pubkey-aaa")
                .queryParam("usrkey", "-1")
                .queryParam("os_flag", "0")
                .build()
                .encode();

        logger.info("url 2 = {}", uriComponents2.toUriString());
        assertEquals(true, uriComponents.toUriString().equals(uriComponents2.toUriString()));
    }

    @Test
    public void testPersist() throws IOException {

        assertNotNull(pantherProperties);
        assertNotNull(adEventRepository);

        ADEvent adEvent = ADEvent.builder().appkey(102830).usrkey(6408038695829506L).osFlag(1)
                .cointInt(3).attpAt(new Timestamp(Instant.now().toEpochMilli()))
                .build();
        logger.info("before save attp. id = {}", adEvent.getId());
        ADEvent saved = pinCruxService.persistADEvent(adEvent);
        Long id = saved.getId();
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(3, saved.getCointInt().intValue());
        assertNull(saved.getCoin());
        logger.info("saved. attt = {}", JsonUtil.toJson(saved));


        saved.setCoin(2);
        saved.setTransid("98b8e01266e192c65cff74a5aec72d32d6438fea");
        saved.setStatus(ADEvent.Status.postback);
        saved.setPostbackAt(new Timestamp(Instant.now().toEpochMilli()));

        ADEvent saved2 = pinCruxService.persistADEvent(saved);
        assertNotNull(saved2);
        assertEquals(2, saved2.getCoin().intValue());
        assertEquals(id.intValue(), saved2.getId().intValue());
        assertEquals(ADEvent.Status.postback, saved2.getStatus());
        assertEquals("postback", saved2.getStatus().name());
        logger.info("saved. postback = {}", JsonUtil.toJson(saved2));
    }

    @Test
    public void testFind() {
        Integer appkey = 102830;
        Long usrkey = 6408038695829506L;
        Integer osFlag = 1;

        ADEvent saved1 = pinCruxService.persistADEvent(ADEvent.builder().appkey(appkey).usrkey(usrkey)
                .osFlag(osFlag).status(ADEvent.Status.attp).build());
        ADEvent saved2 = pinCruxService.persistADEvent(ADEvent.builder().appkey(appkey).usrkey(usrkey)
                .osFlag(osFlag).status(ADEvent.Status.reward).build());
        ADEvent saved3 = pinCruxService.persistADEvent(ADEvent.builder().appkey(appkey).usrkey(usrkey)
                .osFlag(osFlag).status(ADEvent.Status.reward)
                .rewardAt(new Timestamp(Instant.now().toEpochMilli())).build());

        logger.info("id1 = {}", saved1.getId());
        logger.info("id2 = {}", saved2.getId());
        logger.info("id3 = {}", saved3.getId());

        Optional found1 = pinCruxService.findADEventBy(usrkey, appkey, osFlag);

        logger.info("found1 = {}", JsonUtil.toJson(found1.orElse("null")));


        Long usrkey2 = Long.valueOf(6408038695829506L);
        Integer appkey2 = Integer.valueOf(102834);
        Integer osFlag2 = Integer.valueOf(1);
        Optional found2 = pinCruxService.findADEventBy(usrkey2, appkey2, osFlag2);
        assertEquals(false, found2.isPresent());


    }


}
