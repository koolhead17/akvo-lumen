import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import Modal from './Modal';
import Notification from './Notification';

require('../styles/reset.global.scss');
require('../styles/style.global.scss');
require('./Main.scss');
require('fixed-data-table-2/dist/fixed-data-table.css');


function Main({ content, sidebar, notification, loadStatus }) {
  if (loadStatus === 'failed') {
    return (
      <div className="Main">
        <div
          className="failedToLoadMessage"
        >
          <p className="message">
            Failed to load library.
            Please refresh the page and check you are authorised for this tenant.
          </p>
        </div>
        <div className="Main blur">
          {sidebar}
        </div>
      </div>
    );
  }

  return (
    <div className="Main">
      {notification && <Notification {...notification} />}
      {sidebar}
      {content}
      <Modal />
    </div>
  );
}

Main.propTypes = {
  content: PropTypes.object,
  sidebar: PropTypes.object,
  notification: PropTypes.object,
  loadStatus: PropTypes.string,
};

function mapStateToProps(state) {
  return {
    loadStatus: state.loadStatus,
    notification: state.notification,
    modalVisible: state.activeModal != null,
  };
}

export default connect(mapStateToProps)(Main);
