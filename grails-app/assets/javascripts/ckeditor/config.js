/**
 * @license Copyright (c) 2003-2015, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function( config ) {
    //config.skin = 'moonocolor';
    config.language = "es";
    config.uiColor = '#4D76A3';
    config.enterMode = CKEDITOR.ENTER_BR;
//    config.shiftEnterMode = CKEDITOR.ENTER_DIV;

    config.scayt_autoStartup = true;
    config.scayt_sLang = 'es_ES';

    //config.resize_enabled = false;   /* requiere el plugin resize */
    //config.removePlugins = 'elementspath, resize'; /* plugin resize */
    config.extraCss = "body{ min-width:400px; }";
    //config.contentsCss= "p { text-align: justify; margin-bottom: 0; }";

    config.toolbar = [

        { items : ['Source','Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },
        { name: 'editing', items : [ 'Find','Replace','-','SelectAll','-','Scayt' ] },
        { name: 'insert', items : [ 'Image','Table','HorizontalRule','SpecialChar', 'TextColor', 'BGColor'] },
        { name: 'links', items : [ 'Link','Unlink' ] },
        '/',
        { name: 'styles', items : [ 'Format', 'Font',  'FontSize'] },
        { name: 'basicstyles', items : [ 'Bold','Italic', 'Underline', 'Strike','-','RemoveFormat', 'Subscript', 'Superscript' ] },
        { items : [ 'NumberedList','BulletedList','-','Outdent','Indent', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'] },
        //{ name: 'paragraph', items: ['JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'] }
    ];


    /*
     // Toolbar configuration generated automatically by the editor based on config.toolbarGroups.
     config.toolbar = [
     { name: 'document', groups: [ 'mode', 'document', 'doctools' ], items: [ 'Source', '-', 'Save', 'NewPage', 'Preview', 'Print', '-', 'Templates' ] },
     { name: 'clipboard', groups: [ 'clipboard', 'undo' ], items: [ 'Cut', 'Copy', 'Paste', 'PasteText', 'PasteFromWord', '-', 'Undo', 'Redo' ] },
     { name: 'editing', groups: [ 'find', 'selection', 'spellchecker' ], items: [ 'Find', 'Replace', '-', 'SelectAll', '-', 'Scayt' ] },
     { name: 'forms', items: [ 'Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField' ] },
     '/',
     { name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ], items: [ 'Bold', 'Italic', 'Underline', 'Strike', 'Subscript', 'Superscript', '-', 'RemoveFormat' ] },
     { name: 'paragraph', groups: [ 'list', 'indent', 'blocks', 'align', 'bidi' ], items: [ 'NumberedList', 'BulletedList', '-', 'Outdent', 'Indent', '-', 'Blockquote', 'CreateDiv', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock', '-', 'BidiLtr', 'BidiRtl', 'Language' ] },
     { name: 'links', items: [ 'Link', 'Unlink', 'Anchor' ] },
     { name: 'insert', items: [ 'Image', 'Flash', 'Table', 'HorizontalRule', 'Smiley', 'SpecialChar', 'PageBreak', 'Iframe' ] },
     '/',
     { name: 'styles', items: [ 'Styles', 'Format', 'Font', 'FontSize' ] },
     { name: 'colors', items: [ 'TextColor', 'BGColor' ] },
     { name: 'tools', items: [ 'Maximize', 'ShowBlocks' ] },
     { name: 'others', items: [ '-' ] },
     { name: 'about', items: [ 'About' ] }
     ];
     */

    /*
     config.toolbar = [
     [ 'Source', '-', 'Save', 'NewPage', 'Preview', 'Print', '-', 'Templates' ] ,
     [ 'Cut', 'Copy', 'Paste', 'PasteText', 'PasteFromWord', '-', 'Undo', 'Redo' ] ,
     [ 'Find', 'Replace', '-', 'SelectAll', '-', 'Scayt' ],
     [ 'Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField' ],
     '/',
     [ 'Bold', 'Italic', 'Underline', 'Strike', 'Subscript', 'Superscript', '-', 'RemoveFormat' ],
     [ 'NumberedList', 'BulletedList', '-', 'Outdent', 'Indent', '-', 'Blockquote', 'CreateDiv', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock', '-', 'BidiLtr', 'BidiRtl', 'Language' ],
     [ 'Link', 'Unlink', 'Anchor' ] ,
     [ 'Image', 'Flash', 'Table', 'HorizontalRule', 'Smiley', 'SpecialChar', 'PageBreak', 'Iframe' ],
     '/',
     [ 'Styles', 'Format', 'Font', 'FontSize' ],
     [ 'TextColor', 'BGColor' ] ,
     [ 'Maximize', 'ShowBlocks' ],
     [ '-' ] ,
     [ 'About' ]
     ]
     */

};
