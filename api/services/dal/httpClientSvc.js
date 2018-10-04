const axios = require('axios');

module.exports.get = function(endpoint){
    return axios.get(endpoint)
    .then(function (response) {
        return response;
    })
    .catch(function (error) {
        return error;
    });
};