package com.android.sanwei.uikit.cart;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.sanwei.uikit.R;

public class CartAddSubLayout extends LinearLayout {

    private ImageView addIv;
    private ImageView subIv;
    private TextView priceTv;

    private int maxValue;

    private int currentPrice;

    private OnClickListener addOnClickListener;
    private OnClickListener subOnClickListener;

    public CartAddSubLayout(Context context) {
        this(context, null);
    }

    public CartAddSubLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CartAddSubLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_cart_add_and_sub, this);
        addIv = findViewById(R.id.iv_cart_add_and_sub_add);
        subIv = findViewById(R.id.iv_cart_add_and_sub_sub);
        priceTv = findViewById(R.id.iv_cart_add_and_sub_price);

        maxValue = 9999;

        addIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addOnClickListener == null || currentPrice >= maxValue) {
                    return;
                }
                addOnClickListener.onClick(v);
            }
        });

        subIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (subOnClickListener == null || currentPrice <= 0) {
                    return;
                }
                subOnClickListener.onClick(v);
            }
        });
    }

    public void setAddOnClickListener(OnClickListener addOnClickListener) {
        this.addOnClickListener = addOnClickListener;
    }

    public void setSubOnClickListener(OnClickListener subOnClickListener) {
        this.subOnClickListener = subOnClickListener;
    }

    public void removeAddOnClickListener() {
        addOnClickListener = null;
    }

    public void removeSubOnClickListener() {
        subOnClickListener = null;
    }

    public void setPrice(String price) {
        try {
            currentPrice = Integer.parseInt(price);
            if (currentPrice <= 0) {
                currentPrice = 0;
            }
        } catch (Exception exception) {
            currentPrice = 0;
        }
        priceTv.setText(String.valueOf(currentPrice));
    }

    public void setPrice(int price) {
        currentPrice = Math.max(price, 0);
        setPrice(String.valueOf(currentPrice));
    }

    public void setPrice(Integer price) {
        currentPrice = (price == null || price <= 0) ? 0 : price;
        setPrice(currentPrice);
    }


    // 一般来说，加减按钮点击之后，需要先访问网络接口，得到结果才能决定数值显示
    // 故这两个方法用于网络接口成功、失败之后，手动修改数值
    public void countAddOne() {
        if (currentPrice >= maxValue) {
            return;
        }
        currentPrice++;
        setPrice(currentPrice);
    }
    public void countSubOne() {
        if (currentPrice <= 0) {
            return;
        }
        currentPrice--;
        setPrice(currentPrice);
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }
}
