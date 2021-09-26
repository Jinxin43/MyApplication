package com.example.event.db.xEntity;


import com.example.event.model.UploadId;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

import java.io.Serializable;
@Table(name = "uploadEntity")
public class UploadEntity implements UploadId,Serializable {
    @Column(name = "orderNumber", isId = true)
    private String orderNumber;
    @Column(name = "Id")
    private String Id;

    @Override
    public String getOrderNumber() {
        return orderNumber;
    }

    @Override
    public void setOrderNumber(String orderNumber) {
        this.orderNumber=orderNumber;

    }

    @Override
    public String getId() {
        return Id;
    }

    @Override
    public void setId(String id) {
        this.Id=id;

    }
}
