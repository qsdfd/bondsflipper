var ctrl = require('../controllers/bondFlipCtrl');

module.exports = function(app) {
    app.route('/flipdata').get(ctrl.getBondsFlipData);
};
