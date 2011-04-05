/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s): Valery Lezhebokov.
 ************************************************************************
 */
// = OBNotesItems =
//
// Represents the notes section shown in the bottom of the form.
// Note is not shown for new records.
//
isc.ClassFactory.defineClass('OBNoteSectionItem', isc.OBSectionItem);

isc.OBNoteSectionItem.addProperties( {
	// as the name is always the same there should be at most
	// one note section per form
	name : '_notes_',

	overflow : 'hidden',

	canFocus : true,

	prompt : OB.I18N.getLabel('OBUIAPP_NotesPrompt'),

	canvasItem : null,

	visible : true,

	// note formitems don't have an initWidget but an init method
	init : function() {
		// override the one passed in
	this.defaultValue = OB.I18N.getLabel('OBUIAPP_NotesTitle');

	/* tell the form who we are */
	this.form.noteSection = this;

	this.Super('init', arguments);
},

getNotePart : function() {
	if (!this.canvasItem) {
		this.canvasItem = this.form.getField(this.itemIds[0]);
	}
	return this.canvasItem.canvas;
},

setRecordInfo : function(entity, id) {
	this.getNotePart().setRecordInfo(entity, id);
},

refresh : function() {
	this.getNotePart().refresh();
}

});

isc.ClassFactory.defineClass('OBNoteLayout', isc.VLayout);

isc.OBNoteLayout.addProperties( {

			entity : null,

			recordId : null,

			layoutMargin : 0,

			membersMargin : 10,

			noteTextAreaItem : null,

			noteDynamicForm : null,

			saveNoteButton : null,

			noteDSId : '090A37D22E61FE94012E621729090048',

			noteListGrid : null,

			/**
			 * Saves the note to the DB.
			 */
			saveNote : function() {
				this.noteDynamicForm.validate();

				var noteDS = this.getNoteDataSource();

				var currentTime = new Date();

				noteDS.addData( {
							'client' : OB.User.clientId,
							'organization' : OB.User.organizationId,
							'table' : this.getForm().view.standardProperties.inpTableId,
							'record' : this.getForm().view.viewGrid
									.getSelectedRecord().id,
							'note' : this.noteDynamicForm.getField(
									'noteOBTextAreaItem').getValue(),
							'isactive' : 'Y',
							'created' : currentTime,
							'createdBy' : OB.User.id,
							'updated' : currentTime,
							'updatedBy' : OB.User.id
						});

				// clean text area
				this.noteDynamicForm.getItem('noteOBTextAreaItem').clearValue();
				this.evaluateButtonStatus(this.noteDynamicForm.getItem('noteOBTextAreaItem').getValue());
				
			},

			/**
			 * Deletes the note from the DB.
			 */
			deleteNote : function( /* note id to delete */id) {
				var noteDS = this.getNoteDataSource();
				noteDS.removeData( {
					'id' : id
				});
			},
			
			evaluateButtonStatus : function(value){
				if(value === null){
					if(this.saveNoteButton.getState()!=='Disabled'){
						this.saveNoteButton.setDisabled(true);
					}
				}else {
					if(this.saveNoteButton.getState()==='Disabled'){
						this.saveNoteButton.setDisabled(false);
					}
				}
			},

			/**
			 * Returns Notes data source.
			 */
			getNoteDataSource : function() {
				return isc.DataSource.getDataSource(this.noteDSId);
			},

			/**
			 * Initializes the widget.
			 */
			initWidget : function() {
				this.Super('initWidget', arguments);

				// register note DS
				OB.Datasource.get(this.noteDSId);

				var hLayout = isc.HLayout.create( {
					width : '50%',
					height : '100%',
					layoutMargin : 0,
					layoutTopMargin : 10,
					membersMargin : 10
				});
				hLayout.setLayoutMargin();

				this.noteDynamicForm = isc.DynamicForm.create( {
					numCols : 1,
					width : '100%', 
					fields : [ {
						name : 'noteOBTextAreaItem',
						type : 'OBTextAreaItem',
						showTitle : false,
						layout : this,
						width : '*',
						validators : [ {
							type : 'required'
						} ],
						changed : function (form, item, value){
							this.layout.evaluateButtonStatus(value);	
						}
					}]
				});

				this.saveNoteButton = isc.OBFormButton.create( {
					layout : this,
					title : OB.I18N.getLabel('OBUIAPP_SaveNoteButtonTitle'),
					click : 'this.layout.saveNote()'
				});

				hLayout.addMember(this.noteDynamicForm);
				hLayout.addMember(this.saveNoteButton);
				// add the grids to the vertical layout
				this.addMember(hLayout);

				this.noteListGrid = isc.OBGrid
						.create( {
							width : '50%',
							autoFitData: 'vertical',
							fields : [ {
								name : 'colorBar',
								width : '5'
							}, {
								name : 'note'
							} ],
							alternateRecordStyles : false,
							autoFetchData : true,
							baseStyle : 'OBNoteListGridCell',
							dataSource : this.noteDSId,
							fixedRecordHeights : false,
							filterOnKeypress : true,
							headerHeight : 0,
							hoverStyle : 'OBNoteListGridCellOver',
							layout : this,
							selectionType : 'none',
							showEmptyMessage : false,
							styleName : 'OBNoteListGrid',
							wrapCells : true,

							fetchData : function(criteria, callback,
									requestProperties) {
								return this.Super('fetchData', [
										this.convertCriteria(criteria),
										callback, requestProperties ]);
							},

							filterData : function(criteria, callback,
									requestProperties) {
								return this.Super('filterData', [
										this.convertCriteria(criteria),
										callback, requestProperties ]);
							},

							getCriteria : function() {
								var criteria = this.Super('getCriteria',
										arguments) || {};
								criteria = this.convertCriteria(criteria);
								return criteria;
							},

							convertCriteria : function(criteria) {
								criteria = isc.addProperties( {}, criteria || {});

								if (!criteria.criteria) {
									criteria.criteria = [];
								}

								var view = this.layout.getForm().view;

								criteria.criteria.push( {
									fieldName : 'table',
									operator : 'equals',
									value : view.standardProperties.inpTableId
								});

								criteria.criteria
										.push( {
											fieldName : 'record',
											operator : 'equals',
											value : view.viewGrid
													.getSelectedRecord().id
										});

								criteria[OB.Constants.ORDERBY_PARAMETER] = 'updated desc';

								return criteria;

							},

							formatCellValue : function(value, record, rowNum,
									colNum) {

								if (this.getFieldName(colNum) !== 'note') {
									return value;
								}

								value =  value + ' <span class="OBNoteListGridAuthor">' +
										this.getTimePassed(record.created) +
										' ' + OB.I18N.getLabel('OBUIAPP_by') + ' ' +
										record['createdBy._identifier'];

								// show delete link if the note was created by
								// the current user
								if (record.createdBy === OB.User.id) {
									value = value +
											' <span class="OBNoteListGridDelete" >[ <a class="OBNoteListGridDelete" href="#" onclick="' +
											this.layout.ID + '.deleteNote(\'' +
											record.id +
											'\')">' + OB.I18N.getLabel('OBUIAPP_delete') + '</a> ]</span>';
								} 
								value = value + '</span>';
								return value;
							},

							getTimePassed : function(
							/* date the note was created */created) {
								// 0-59 minutes: minutes
							// 1-24 hours: hours
							// >24 hours: days
							// >7 days: weeks
							// >30 days: months

							var now = new Date(), 
							    msCreated = created.getTime(),
							    msNow = now.getTime(),
							    n;

							// time difference in days
							var diffDays = Math.floor((msNow - msCreated) / (1000 * 60 * 60 * 24));
							if (diffDays >= 30) {
								n = Math.floor(diffDays / 30);
								return this.formatTimePassedMessage(n,
										'OBUIAPP_months_ago');
							} else if (diffDays >= 7) {
								n = Math.floor(diffDays / 7);
								return this.formatTimePassedMessage(n,
										'OBUIAPP_weeks_ago');
							} else if (diffDays >= 1) {
								n = diffDays;
								return this.formatTimePassedMessage(n,
										'OBUIAPP_days_ago');
							}

							// time difference in hours
							var diffHours = Math.floor((msNow - msCreated) / (1000 * 60 * 60));
							if (diffHours >= 1) {
								n = diffHours;
								return this.formatTimePassedMessage(n,
										'OBUIAPP_hours_ago');
							}

							// time difference in minutes
							n = Math.floor((msNow - msCreated) / (1000 * 60));
							return this.formatTimePassedMessage(n,
									'OBUIAPP_minutes_ago');
						},

						formatTimePassedMessage : function(
						/* number of time units */n, /*
														 * message id
														 */messageId) {
							var message = OB.I18N.getLabel(messageId);
							return message.replace(/^N/, n);
						},

						getBaseStyle : function(record, rowNum, colNum) {
							if (this.getFieldName(colNum) !== 'colorBar') {
								return this.baseStyle;
							}

							if (record.createdBy === OB.User.id) {
								// XXX what CSS may I use here? or I need to
							// create one?
							return 'OBNoteListGridCurrentUserNoteCell';
						} else {
							// XXX what CSS may I use here? or I need to create
							// one?
							return 'OBNoteListGridOtherUserNoteCell';
						}
					}

						});

				this.addMember(this.noteListGrid);

			},

			/**
			 * Sets record information.
			 */
			setRecordInfo : function(entity, id) {
				this.entity = entity;
				this.recordId = id;
			},

			refresh : function() {
				this.noteDynamicForm.getItem('noteOBTextAreaItem').clearValue();
				this.evaluateButtonStatus(this.noteDynamicForm.getItem('noteOBTextAreaItem').getValue());
				this.noteListGrid.fetchData();
			},

			getForm : function() {
				return this.canvasItem.form;
			}

		});

isc.ClassFactory.defineClass('OBNoteCanvasItem', isc.CanvasItem);

isc.OBNoteCanvasItem.addProperties( {

	canFocus : true,

	// setting width/height makes the canvasitem to be hidden after a few
	// clicks on the section item, so don't do that for now
	// width: '100%',
	// height: '100%',
	showTitle : false,
	overflow : 'auto',
	// note that explicitly setting the canvas gives an error as not
	// all props are set correctly on the canvas (for example the
	// pointer back to this item: canvasItem
	// for setting more properties use canvasProperties, etc. see
	// the docs
	canvasConstructor : 'OBNoteLayout',

	// never disable this one
	isDisabled : function() {
		return false;
	}

});