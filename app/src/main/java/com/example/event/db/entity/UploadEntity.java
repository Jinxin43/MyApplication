package com.example.event.db.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.event.model.UploadId;
import java.io.Serializable;
@Entity(tableName = "uploadEntity", indices = {
        @Index(value = "orderNumber")
})
public class UploadEntity implements UploadId,Serializable {
    @PrimaryKey
    private String orderNumber;
    private  String Id;


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
