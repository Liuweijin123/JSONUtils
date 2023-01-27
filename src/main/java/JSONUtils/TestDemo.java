package JSONUtils;

import JSONUtils.JSONParserProvider.BaseParserProvider;
import JSONUtils.JSONParserProvider.JacksonProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TestDemo {

    public static void main(String[] args) {
        JacksonProvider.setDefaultProviderName("gson");
        String snapshoot = "[{\n" +
                "\t\"orientation\": 270,\n" +
                "\t\"image_size\": [1685, 1191],\n" +
                "\t\"extra\": {},\n" +
                "\t\"details\": {\n" +
                "\t\t\"date\": \"2020年05月15日\",\n" +
                "\t\t\"number\": \"10705998\",\n" +
                "\t\t\"station_getoff\": \"昆明\",\n" +
                "\t\t\"total\": \"55.00\",\n" +
                "\t\t\"code\": \"153001924931\",\n" +
                "\t\t\"user_id\": \"*0033\",\n" +
                "\t\t\"kind\": \"交通\",\n" +
                "\t\t\"station_geton\": \"玉溪\",\n" +
                "\t\t\"name\": \"冯林\",\n" +
                "\t\t\"time\": \"\",\n" +
                "\t\t\"title\": \"云南通用机打发票\",\n" +
                "\t\t\"producer_stamp\": \"国家税务总局\"\n" +
                "\t},\n" +
                "\t\"page\": 0,\n" +
                "\t\"type\": \"10505\",\n" +
                "\t\"region\": [81, 214, 828, 903]\n" +
                "}]";
        JSONNode json = JacksonProvider.getDefaultProvider().parse(snapshoot);
        System.out.println(json.toJson());
        JSONViewer root = JSONViewer.of(json);
        System.out.println(root.node("0").node("title").isSuccess() + ";");
        System.out.println(root.findFirstByNodeName("title", "云南通用机打发票").toString() + ";");
        System.out.println(root.findFirstByNodeName("title", null).toString() + ";");
        System.out.println(root.findFirstByNodeName("title", "").toString() + ";");
        System.out.println(root.path("/0/details/code").toString() + ";");
        System.out.println(root.node("0").node("type").path("/0/details/code").toString() + ";");
        System.out.println(root.node("0").node("type").path("../details/code").toString() + ";");
        JSONViewer detail = root.node("0").node("details");
        for (JSONViewer child : root.getChildren()) {
            System.out.println(child.toString() + ";");
        }
        for (JSONViewer child : detail.getChildren()) {
            System.out.println(child.toString() + ";");
        }
        String val = detail.node("code").getValStr();
        System.out.println(val);
        System.out.println(detail.node("afsfa").isLeaf());
        System.out.println(detail.node("afsfa").getParent().getPath());
        System.out.println(detail.node("afsfa").getPath());

        System.out.println("findFirstByJson:" + root.findFirstByJson("{\"number\":\"10705998\",\"kind\": \"交通\"}").toString() + ";");
        System.out.println("findByJson:" + root.findByJson("{\"details\": {\"number\":\"10705998\",\"kind\": \"交通\"},\"type\": \"10505\"}").get(0).toString() + ";");
        System.out.println("findToBeamByObj:" + BaseParserProvider.getDefaultProvider().toJson(root.findToBeamByObj(new TestClass().setKind("交通"))) + ";");
        System.out.println("findToListByObj:" + BaseParserProvider.getDefaultProvider().toJson(root.findToListByObj(new TestClass().setKind(".*"))) + ";");//条件值支持正值表达式

        Integer val2 = (Integer) detail.root().node("0").node("region").node("0").getVal();
        System.out.println(val2);


        String testJson = "{\"instanceId\":\"dc9dd07cfcdf47d8a483aed653f839be\",\"transferVoucher\":\"{\\\"updateDate\\\":1657175524000,\\\"procInstId\\\":\\\"dc9dd07cfcdf47d8a483aed653f839be\\\",\\\"sort\\\":0,\\\"type\\\":0,\\\"procDefId\\\":\\\"Pro_b69a76c91c90487ea6118352d5662857\\\",\\\"number\\\":\\\"positiveNumber\\\",\\\"money\\\":\\\"223\\\",\\\"formula\\\":\\\"\\\\\\\"普通税金\\\\\\\"+\\\\\\\"OCR税金\\\\\\\"\\\",\\\"ocrOrSubFormValue\\\":\\\"{\\\\\\\"field_1654588316400\\\\\\\":100,\\\\\\\"field_1654588327352\\\\\\\":100,\\\\\\\"field_1654588718495\\\\\\\":\\\\\\\"\\\\\\\",\\\\\\\"field_1654595187266\\\\\\\":\\\\\\\"游侠\\\\\\\",\\\\\\\"field_1654588692368\\\\\\\":\\\\\\\"32f274295cc04458aba151b402c282fa\\\\\\\",\\\\\\\"field_1657160310939\\\\\\\":\\\\\\\"S.16.XJB.TG.192,Annual Dinner Drinks,Party Beer,LTI\\\\\\\",\\\\\\\"field_1656924303492\\\\\\\":\\\\\\\"3fe31bce79ba4fb8a7b88093a2b3494c\\\\\\\"}\\\",\\\"id\\\":\\\"0b929f6afa7a4170947039756490b173\\\",\\\"subjectCode\\\":\\\"10359\\\",\\\"subjectName\\\":\\\"借方科目名称1\\\",\\\"createDate\\\":1657175524000}\"}";
        JSONViewer root2 = new JSONViewer(testJson);
        System.out.println(root2.node("transferVoucher").getValStr());
        System.out.println(root2.findFirstByNodeName("instanceId").getValStr());
        System.out.println(root2.findFirstByNodeName("transferVoucher", null).getValStr());
        root2 = root2.findFirstByNodeName("transferVoucher", null).getAndConvertTo();
        System.out.println(root2.findFirstByNodeName("ocrOrSubFormValue", null).getValStr());
        System.out.println(root2.findFirstByNodeName("subjectName").getValStr());
        root2 = root2.findFirstByNodeName("ocrOrSubFormValue").getAndConvertTo();
        System.out.println(root2.findFirstByNodeName("field_1654595187266", null).getValStr());
        System.out.println(root2.findFirstByNodeName("field_1654595187266", "游侠").getParent().node("field_1656924303492").getValStr());
        System.out.println(root2.findFirstByNodeName("field_1654595187266", "游侠").getParent().getName());
        System.out.println(root2.filter(t -> t.node("field_1654595187266").valEqualStr("游侠")).get(0).getName());
        System.out.println(root2.filterFirst(t -> t.node("field_1654595187266").valEqualStr("游侠")).getName());


        String testJson3 = "[{\"backStartName\":\"退回发起人\",\"sort\":0,\"type\":\"start\",\"backLastName\":\"退回上一个审批节点\",\"backStart\":false,\"modifyTime\":1657095209000,\"createTime\":1657095209000,\"templateNodeId\":\"eb3d34dc799541ed971a1b6d4c09dfdb\",\"name\":\"开始\",\"coerceEnd\":false,\"backLast\":false,\"id\":\"5e5ea2aed1074609b43bef358e40f7f9\",\"runId\":\"76e82336200e48d2965c797b1ab03a7b\",\"coerceEndName\":\"退回发起人\",\"status\":1},{\"backStartName\":\"退回发起人\",\"sort\":1,\"type\":\"userTask\",\"backLastName\":\"不同意\",\"backStart\":true,\"operate\":\"同意 Agree\",\"modifyTime\":1657095218000,\"createTime\":1657095209000,\"templateNodeId\":\"ee443ae77d464caeb6628e97cf721695\",\"name\":\"审批节点\",\"coerceEnd\":false,\"backLast\":false,\"id\":\"606642cbb8154d8da266f127bcb1adc6\",\"runId\":\"76e82336200e48d2965c797b1ab03a7b\",\"coerceEndName\":\"强制结束流程\",\"status\":1},{\"backStartName\":\"退回发起人\",\"sort\":2,\"type\":\"userTask\",\"backLastName\":\"不同意\",\"backStart\":true,\"operate\":\"同意 Agree\",\"modifyTime\":1657095224000,\"createTime\":1657095218000,\"templateNodeId\":\"57c7b78b155f447ab874e1591b213458\",\"name\":\"审批节点1\",\"coerceEnd\":false,\"backLast\":false,\"id\":\"c550f476eb6b4f2bb813d75b6ec35edb\",\"runId\":\"76e82336200e48d2965c797b1ab03a7b\",\"coerceEndName\":\"强制结束流程\",\"status\":1},{\"backStartName\":\"退回发起人\",\"sort\":3,\"type\":\"end\",\"backLastName\":\"退回上一个审批节点\",\"backStart\":false,\"modifyTime\":1657095224000,\"createTime\":1657095224000,\"templateNodeId\":\"a09173dfa42e43538a3b22421c16a5a4\",\"name\":\"结束\",\"coerceEnd\":false,\"backLast\":false,\"id\":\"e2dd1e835b064df3bc75a624f2a5cad6\",\"runId\":\"76e82336200e48d2965c797b1ab03a7b\",\"coerceEndName\":\"退回发起人\",\"status\":1}]";
        JSONViewer root3 = new JSONViewer(testJson3);
        System.out.println(new Date((Long) root3.findFirstByNodeName("type", "end").getParent().node("createTime").getVal()));


        String testJson4 = "{\"seller\":\"肇庆京东盛甲贸易有限公司\",\"updateDate\":1657099372000,\"code\":\"04400203321172167165\",\"tax\":42.39,\"type\":\"10503\",\"buyer\":\"吕子丽\",\"jsonInfo\":\"{\\\"date\\\":\\\"2021年09月13日\\\",\\\"seller\\\":\\\"肇庆京东盛甲贸易有限公司\\\",\\\"vehicle_mark\\\":\\\"0\\\",\\\"code\\\":\\\"044002033211\\\",\\\"company_seal\\\":\\\"1\\\",\\\"city\\\":\\\"\\\",\\\"remark\\\":\\\"订单号:221221569841\\\",\\\"form_type\\\":\\\"\\\",\\\"item_names\\\":\\\"*其他机械设备*电器电子产品及配件,*其他机械设备*电器电子产品及配件\\\",\\\"title\\\":\\\"广东增值税电子普通发票\\\",\\\"issuer\\\":\\\"王梅\\\",\\\"stamp_info\\\":\\\"肇庆京东盛甲贸易有限公司,91441203MA51UJEM11\\\",\\\"seller_tax_id\\\":\\\"91441203MA51UJEM11\\\",\\\"buyer_tax_id\\\":\\\"\\\",\\\"company_seal_mark\\\":\\\"1\\\",\\\"number\\\":\\\"72167165\\\",\\\"total_cn\\\":\\\"叁佰陆拾捌圆肆角\\\",\\\"total\\\":\\\"368.40\\\",\\\"province\\\":\\\"广东省\\\",\\\"check_code\\\":\\\"61305875610677483316\\\",\\\"pretax_amount\\\":\\\"326.01\\\",\\\"seller_bank_account\\\":\\\"中国银行股份有限公司肇庆鼎湖支行695170410217\\\",\\\"producer_stamp\\\":\\\"国家税务总局\\\",\\\"ciphertext\\\":\\\"-905-002+46810/31>03/2+5524,-5<3<5201*/036972<8>099>1-5,3317*9-<-6<<57651*454/070*4,10+0+713<5201*/036972<8998/\\\",\\\"code_confirm\\\":\\\"044002033211\\\",\\\"electronic_mark\\\":\\\"1\\\",\\\"kind\\\":\\\"数码电器\\\",\\\"service_name\\\":\\\"其他机械设备\\\",\\\"tax\\\":\\\"42.39\\\",\\\"reviewer\\\":\\\"李思\\\",\\\"buyer\\\":\\\"吕子丽\\\",\\\"machine_code\\\":\\\"661814564268\\\",\\\"receiptor\\\":\\\"王陆\\\",\\\"seller_addr_tel\\\":\\\"肇庆市鼎湖区桂城新城北八区肇庆新区投资发展有限公司厂房(B幢)350室020-22165500\\\",\\\"items\\\":[{\\\"total\\\":\\\"618.58\\\",\\\"unit\\\":\\\"\\\",\\\"quantity\\\":\\\"1\\\",\\\"price\\\":\\\"618.58\\\",\\\"name\\\":\\\"*其他机械设备*电器电子产品及配件\\\",\\\"specification\\\":\\\"\\\",\\\"tax\\\":\\\"80.42\\\",\\\"tax_rate\\\":\\\"13%\\\"},{\\\"total\\\":\\\"-292.57\\\",\\\"unit\\\":\\\"\\\",\\\"quantity\\\":\\\"\\\",\\\"price\\\":\\\"\\\",\\\"name\\\":\\\"*其他机械设备*电器电子产品及配件\\\",\\\"specification\\\":\\\"\\\",\\\"tax\\\":\\\"-38.03\\\",\\\"tax_rate\\\":\\\"13%\\\"}]}\",\"total\":368.4,\"id\":\"7f76ebd9b4a05b5dc59949e24ad5f5d9\",\"runId\":\"76e82336200e48d2965c797b1ab03a7b\",\"issueDate\":\"2021年09月13日\",\"status\":1,\"createDate\":1657078088000}";
        JSONViewer root4 = new JSONViewer(testJson4);
        System.out.println(root4.node("type").getValStr());


        JSONViewer root5 = new JSONViewer();
        root5.setVal(1);
        System.out.println(root5.getValStr());
        root5.node("leyer1").setVal("layer1 value");
        System.out.println(root5.toString());
        System.out.println(root5.node("leyer1").node("layer2").node("layer3").setVal("layer3 value"));
        root5.node("leyer1").node("leyer2_1").setVal("layer2_1 value");
        System.out.println(root5);
        root5.node("leyer1").node("layer2").node("layer3_2").setVal("layer3_2 value");
        root5.node("leyer1").node("leyer2_1").setVal("layer2_1_2 value");
        System.out.println(root5.toString());
        System.out.println(root5.findFirstByNodeName("layer3").getValStr());


        JSONViewer root6 = new JSONViewer();
        root6.node("layer1").setVal(new ArrayList<String>());
        root6.node("layer1").node("0").setVal(0);
        root6.node("layer1").node("1").setVal(1);
        root6.node("layer1").node("2").setVal(2);
        root6.node("layer1").node("3").setVal(3);
        System.out.println(root6);
        List<JSONViewer> array6 = root6.node("layer1").getChildren();
        root6.node("layer1").setVal(array6);
        System.out.println(root6);
        root6.node("layer1").node("4").setVal("4");
        root6.node("layer1").node("5").node("layer2").setVal("layer1[5].layer2 value");
        System.out.println(root6);
//        root6.node("layer1").node("layer2").setVal(root6.node("layer1").getVal()); //递归引用会溢出
        root6.node("layer1").node("layer2").setVal(root6.node("layer1").copyNew().getVal());
        root6.node("layer1").node("layer2_1").setVal("layer2_1 value");
        System.out.println(root6);

        JSONViewer root7 = new JSONViewer(new ArrayList<>());
        root7.node("0").node("orientation").setVal(270);
        root7.node("0").node("image_size").setVal(Arrays.asList(1685, 1191));
        root7.node("0").node("extra").setVal(new JSONViewer());
        root7.node("0").node("details").node("date").setVal("2020年05月15日").getParent().node("number").setVal("10705998");
        root7.node("0").node("page").setVal(0);
        root7.node("0").node("type").setVal("10505");
        root7.node("0").node("region").setVal(Arrays.asList(81, 214, 828, 903));
        System.out.println(root7);
        root7.findFirstByNodeName("details").node("name").setVal("冯林");
        System.out.println(root7);

        JSONViewer root8 = new JSONViewer(root7.toString());
        root8.node("0").node("details").node("date").setVal("2020年05月16日");
        JSONViewer details8 = root8.node("0").node("details");
        details8.node("station_geton").setVal("玉溪");
        details8.node("total").setVal(0);
        root8.findFirstByNodeName("details").node("kind").setVal("交通");
        root8.findFirstByNodeName("total").setVal("55");
        System.out.println(root8);

        String testJson9 = "{\"ruleOption1\":{\"processStatus\":\"pass\",\"judge\":\"contains\",\"fieldId\":\"\",\"fieldOption\":\"\",\"mainRule\":\"designate\",\"designateRule\":\"other\",\"designateValue\":\"lastNodeName\",\"customizeValue\":\"\"},\"ruleOption2\":{\"processStatus\":\"approval\",\"judge\":\"uncontains\",\"fieldId\":\"\",\"fieldOption\":\"\",\"mainRule\":\"designate\",\"designateRule\":\"other\",\"designateValue\":\"notApprovedOrNotRead\",\"customizeValue\":\"\"}}";
        JSONViewer root9 = new JSONViewer(testJson9);
        root9.findFirstByNodeName("ruleOption1").node("judge").setVal("contains");
        root9.findFirstByNodeName("ruleOption2").node("judge").setVal("unContains");
        for (JSONViewer child : root9.getChildren()) {
            child.node("value1").setVal("value1");
            child.node("value2").setVal("value2");
        }
        System.out.println(root9);

        String testJson10 = "{\"nodeValue\":\"\",\"designateValue\":\"processNo\",\"customizeValue\":\"\",\"mainRule\":\"designate\",\"designateRule\":\"other\"}";
        JSONViewer root10 = new JSONViewer(testJson10);
        List<JSONViewer> nodes10 = root10.filter(
                t -> t.node("mainRule").isSuccess()
        );
        System.out.println(nodes10.size());

    }

    private static class TestClass {
        private String number;
        private String code;
        private String kind;

        public String getNumber() {
            return number;
        }

        public TestClass setNumber(String number) {
            this.number = number;
            return this;
        }

        public String getCode() {
            return code;
        }

        public TestClass setCode(String code) {
            this.code = code;
            return this;
        }

        public String getKind() {
            return kind;
        }

        public TestClass setKind(String kind) {
            this.kind = kind;
            return this;
        }
    }
}
