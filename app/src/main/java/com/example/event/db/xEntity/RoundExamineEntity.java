package com.example.event.db.xEntity;



import com.example.event.model.RoundExamine;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
import java.util.List;

@Table(name = "roundExamine")
public class RoundExamineEntity implements RoundExamine, Serializable {
    @Column(name = "orderNumber", isId = true)
    private String orderNumber;
    @Column(name = "sheng")
    private String sheng;
    @Column(name = "xian")
    private String xian;
    @Column(name = "address")
    private String address;
    @Column(name = "exmainPerson")
    private String exmainPerson;
    @Column(name = "fillPerson")
    private String fillPerson;
    @Column(name = "examineDate")
    private String examineDate;
    @Column(name = "zhongCName")
    private String zhongCName;
    @Column(name = "shuCName")
    private String shuCName;
    @Column(name = "keCName")
    private String keCName;
    @Column(name = "zhongLaName")
    private String zhongLaName;
    @Column(name = "shuLaName")
    private String shuLaName;
    @Column(name = "keLaName")
    private String keLaName;
    @Column(name = "latitude")
    private double latitude;
    @Column(name = "longtitude")
    private double longtitude;
    @Column(name = "hight")
    private double hight;
    @Column(name = "poXiang")
    private String poXiang;
    @Column(name = "poWei")
    private String poWei;
    @Column(name = "poDu")
    private String poDu;
    @Column(name = "treeHight")
    private int treeHight;
    @Column(name = "XiongJin")
    private int XiongJin;
    @Column(name = "GuanFu")
    private int GuanFu;
    @Column(name = "zhiHight")
    private double zhiHight;
    @Column(name = "Xuji")
    private double Xuji;
    @Column(name = "TuType")
    private String TuType;
    @Column(name = "importDescribe")
    private String importDescribe;
    @Column(name = "photoOrderNum")
    private String photoOrderNum;
    @Column(name = "takePerson")
    private String takePerson;
    @Column(name = "takeDate")
    private String takeDate;
    @Column(name = "stateDescribe")
    private String stateDescribe;
    @Column(name = "photoList")
    private String photoList;
//    @Column(name = "videoList")
//    private String videoList;
//    @Column(name = "videoThumList")
//    private String videoThumList;


    @Override
    public String getOrderNumber() {
        return orderNumber;
    }

    @Override
    public void setOrderNumber(String orderNumber) {
        this.orderNumber=orderNumber;
    }

    @Override
    public String getSheng() {
        return sheng;
    }

    @Override
    public void setSheng(String sheng) {
        this.sheng=sheng;
    }

    @Override
    public String getXian() {
        return xian;
    }

    @Override
    public void setXian(String xian) {
        this.xian=xian;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setAddress(String address) {
        this.address=address;
    }

    @Override
    public String getExmainPerson() {
        return exmainPerson;
    }

    @Override
    public void setExmainPerson(String exmainPerson) {
        this.exmainPerson=exmainPerson;
    }

    @Override
    public String getFillPerson() {
        return fillPerson;
    }

    @Override
    public void setFillPerson(String fillPerson) {
        this.fillPerson=fillPerson;
    }

    @Override
    public String getExamineDate() {
        return examineDate;
    }

    @Override
    public void setExamineDate(String examineDate) {
        this.examineDate=examineDate;
    }

    @Override
    public String getZhongCName() {
        return zhongCName;
    }

    @Override
    public void setZhongCName(String zhongCName) {
        this.zhongCName=zhongCName;
    }

    @Override
    public String getShuCName() {
        return shuCName;
    }

    @Override
    public void setShuCName(String shuCName) {
        this.shuCName=shuCName;
    }

    @Override
    public String getKeCName() {
        return keCName;
    }

    @Override
    public void setKeCName(String keCName) {
        this.keCName=keCName;
    }

    @Override
    public String getZhongLaName() {
        return zhongLaName;
    }

    @Override
    public void setZhongLaName(String zhongLaName) {
        this.zhongLaName=zhongLaName;
    }

    @Override
    public String getShuLaName() {
        return shuLaName;
    }

    @Override
    public void setShuLaName(String shuLaName) {
        this.shuLaName=shuLaName;
    }

    @Override
    public String getKeLaName() {
        return keLaName;
    }

    @Override
    public void setKeLaName(String keLaName) {
        this.keLaName=keLaName;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public void setLatitude(double latitude) {
        this.latitude=latitude;
    }

    @Override
    public double getLongtitude() {
        return longtitude;
    }

    @Override
    public void setLongtitude(double longtitude) {
        this.longtitude=longtitude;
    }

    @Override
    public double getHight() {
        return hight;
    }

    @Override
    public void setHight(double hight) {
        this.hight=hight;
    }

    @Override
    public String getPoXiang() {
        return poXiang;
    }

    @Override
    public void setPoXiang(String poXiang) {
        this.poXiang=poXiang;
    }

    @Override
    public String getPoWei() {
        return poWei;
    }

    @Override
    public void setPoWei(String poWei) {
        this.poWei=poWei;
    }

    @Override
    public String getPoDu() {
        return poDu;
    }

    @Override
    public void setPoDu(String poDu) {
        this.poDu=poDu;
    }

    @Override
    public int getTreeHight() {
        return treeHight;
    }

    @Override
    public void setTreeHight(int treeHight) {
        this.treeHight=treeHight;
    }

    @Override
    public int getXiongJin() {
        return XiongJin;
    }

    @Override
    public void setXiongJin(int xiongJin) {
        this.XiongJin=xiongJin;
    }

    @Override
    public int getGuanFu() {
        return GuanFu;
    }

    @Override
    public void setGuanFu(int guanFu) {
        this.GuanFu=guanFu;
    }

    @Override
    public double getZhiHight() {
        return zhiHight;
    }

    @Override
    public void setZhiHight(double zhiHight) {
        this.zhiHight=zhiHight;
    }

    @Override
    public double getXuji() {
        return Xuji;
    }

    @Override
    public void setXuji(double xuji) {
        this.Xuji=xuji;
    }

    @Override
    public String getTuType() {
        return TuType;
    }

    @Override
    public void setTuType(String tuTyp) {
        this.TuType=tuTyp;
    }

    @Override
    public String getImportDescribe() {
        return importDescribe;
    }

    @Override
    public void setImportDescribe(String importDescribe) {
        this.importDescribe=importDescribe;
    }

    @Override
    public String getPhotoOrderNum() {
        return photoOrderNum;
    }

    @Override
    public void setPhotoOrderNum(String photoOrderNum) {
        this.photoOrderNum=photoOrderNum;
    }

    @Override
    public String getTakePerson() {
        return takePerson;
    }

    @Override
    public void setTakePerson(String takePerson) {
        this.takePerson=takePerson;
    }

    @Override
    public String getTakeDate() {
        return takeDate;
    }

    @Override
    public void setTakeDate(String takeDate) {
        this.takeDate=takeDate;
    }

    @Override
    public String getStateDescribe() {
        return stateDescribe;
    }

    @Override
    public void setStateDescribe(String stateDescribe) {
        this.stateDescribe=stateDescribe;
    }

    @Override
    public String getPhotoList() {
        return photoList;
    }

    @Override
    public void setPhotoList(String photoList) {
        this.photoList = photoList;
    }

//    @Override
//    public String getVideoList() {
//        return videoList;
//    }
//
//    @Override
//    public void setVideoList(String videoList) {
//        this.videoList = videoList;
//    }
//
//    @Override
//    public String getVideoThumList() {
//        return videoThumList;
//    }
//
//    @Override
//    public void setVideoThumList(String videoThumList) {
//         this.videoThumList=videoThumList;
//    }


}
