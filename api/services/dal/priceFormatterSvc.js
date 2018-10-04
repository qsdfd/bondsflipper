const responseGetter = require('./priceRawResponseGetterSvc.js');

responseGetter.getWebPageResponse()
.then(function(res){
    let obj = res.response.data;
    // console.log(Object.keys(obj));
    console.log(obj);
})
.catch(function(err){
    console.log(err)
});