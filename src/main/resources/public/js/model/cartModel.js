/**
 * data of the basket
 */
var cartModel = {

    // Buy Now
    add : function (data, success) {
        czHttp.getJSON('./data/success.json', data, function (responseData) {
            if(responseData.isok){
                success(responseData);
            }
        });
    },

    // delete the product in basket
    remove : function (data, success) {
        czHttp.getJSON('./data/success.json', data, function (responseData) {
            if(responseData.isok){
                success(responseData);
            }
        });
    },

    // change the amount of products
    changeNumber : function (data, success) {
        czHttp.getJSON('./data/success.json', data, function (responseData) {
            if(responseData.isok){
                success(responseData);
            }
        });
    },

    // statistics of the basket
    subtotal : function (success) {
        czHttp.getJSON('./data/orders.json', data, function (responseData) {
            if(responseData.isok){
                success(responseData);
            }
        });
    },

    // basket list
    list : function (success) {

        czHttp.getJSON('./data/orders.json', {}, function(responseData){
            success(responseData);
        });
    }
};