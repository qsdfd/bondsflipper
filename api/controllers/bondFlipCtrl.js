var svc = require('../services/bll/bondFlipSvc');

module.exports.getBondsFlipData = function(req, res){
  svc.getBondsFlipData()
  .then(result => {
    res.json(result);
  })
  .catch(err => {
    res.json(err);
  });
};