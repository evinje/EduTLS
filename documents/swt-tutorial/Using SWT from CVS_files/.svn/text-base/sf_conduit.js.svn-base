spsupport.p = {
    sfDomain :  superfish.b.pluginDomain,
    imgPath :  superfish.b.pluginDomain.replace( "https", "http") + "images/",
    cdnUrl : superfish.b.cdnUrl,
    appVersion : superfish.b.appVersion,
    clientVersion : superfish.b.clientVersion,
    site :  superfish.b.pluginDomain,
    isIEQ : ( +document.documentMode == 5 ? 1 : 0 ),
    sfIcon : {
        maxSmImg : {
            w : 88,
            h : 70
        },
        icons : [],
        big : {
            w : 64,
            h : 48
        },
        small : {
            w : 42,
            h : 38
        },
        prog : {
            time : 1000,
            node : 0,
            color : "#398AFD",
            opac  : "0.3",
            w : [ 62, 40 ],
            h : 13
        }
    },

    onFocus : -1,
    psuHdrHeight : 22,
    psuRestHeight : 26,
    oopsTm : 0,
    iconTm : 0,
    dlsource : superfish.b.dlsource,
    w3iAFS : superfish.b.w3iAFS,
    CD_CTID: superfish.b.CD_CTID,
    userid: superfish.b.userid,
    statsReporter: superfish.b.statsReporter,
    minImageArea : ( 60 * 60 ),
    aspectRatio : ( 1.0/2.0 ),
    supportedSite : 0,
    ifLoaded : 0,
    ifExLoading: 0,
    overIcon: 0,
    statSent : 0,

    icons: 0,
    partner : ( superfish.b.partnerCustomUI ? superfish.b.images + "/" : "" ),

    prodPage: {
        s: 0,   // sent - first request
        i: 0,   // images count
        p: 0,   // product image
        e: 0,   // end of session slideup session
        d: 210, // dimension of image
        l: 1000 // line - in px from top
    },
    before : -1  // Close before
};
spsupport.api = {
    jsonpRequest: function(url, data, successFunc, errorFunc, callBack){
        try{
            if( callBack == null ){
                var date = new Date();
                callBack = "superfishfunc" + date.getTime();
            }
            window[callBack] = function(json) {
                if(successFunc != null)
                    successFunc(json);
            };
            sufio.io.script.get({
                url: url + ( url.indexOf("?") > -1 ? "&" : "?" ) + "callback=" + callBack,
                content: data,
                load : function(response, ioArgs) {
                    window[callBack]  = null;
                },
                error : function(response, ioArgs) {
                    window[callBack]  = null;
                    if(errorFunc != null)
                        errorFunc( response, ioArgs);
                }
            });
        }
        catch(ex){
        }
    },

    toPCase: function( s ){
        return s.replace(/\w\S*/g, function(t){
            return t.charAt(0).toUpperCase() + t.substr(1).toLowerCase();
        });
    },
    getDomain: function(){
        var dD = document.location.host;
        var dP = dD.split(".");
        var len = dP.length;
        if ( len > 2 ){
            var co = ( dP[ len - 2 ] == "co" ? 1 : 0 );
            dD = ( co ? dP[ len - 3 ] + "." : "" ) + dP[ len - 2 ] + "." + dP[ len - 1 ];
        }
        return dD;
    },
    validDomain: function(){
        try{
            var d = document;
            if( d == null || d.domain == null ||
                d == undefined || d.domain == undefined || d.domain == ""
                || d.location == "about:blank" || d.location == "about:Tabs"
                || d.location.toString().indexOf( "superfish.com/congratulation.jsp" ) > -1  ){
                return false;
            }else{
                return (/^([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,5}$/).test( d.domain );
            }
        }catch(e){
            return false;
        }
    },

    init: function(){
        var spp = spsupport.p;
        if( window.sufio )
            return;

        if( !spsupport.api.validDomain() )
            return;

        spsupport.api.dojoReady = false;

        if ( ! top.djConfig ){
            djConfig = {}
        }
        djConfig.afterOnLoad = true;
        // djConfig.baseUrl = spp.cdnUrl;
        djConfig.baseScriptUri = spp.cdnUrl;
        djConfig.useXDomain = true;
        djConfig.scopeMap = [ ["dojo", "sufio"], ["dijit", "sufiw"], ["dojox", "sufix"] ];
        djConfig.require = ["dojo.io.script" ,"dojo._base.html", "dojo.window"];
        djConfig.modulePaths =  {
            "dojo": spp.cdnUrl + "dojo",
            "dijit": spp.cdnUrl + "dijit",
            "dojox": spp.cdnUrl + "dojox"
        };

        superfish.b.inj(  spp.cdnUrl + "dojo/dojo.xd.js",
            1,1,
            function(){
                sufio.addOnLoad(function(){
                    spsupport.api.dojoLoaded();
                });
            });

    },
    gotMessage: function( param, from ){
        if(from && from.indexOf("superfish.com") == -1 ){
            return;
        }
        if (param == 300){
            conduitToolbarCB("openWindow");
          return;
        }

        if ( param ){
            param = param + "";
            var prep = param.split( "|" );
        }

        var fromPsu = ( prep.length > 2 );
        if ( fromPsu ) {
            if (spsupport.p.prodPage.e) {
                return;
            }
        }

        param = ( +prep[ 0 ] );

        var sfu = superfish.util;
        var sfp = spsupport.p;

        if( param == 101 ){ // sys down
            sfu.sysDown();
        }
        else{
            if( param == -7890 ){ // init
                sfp.ifLoaded = 1;
                if( sfu && sfu.standByData != 0 ){
                    sfu.sendRequest( sfu.standByData );
                //sfu.standByData = 0;
                }
            }
            else if( param >= 200 ){
                // 20 -  ( slideup paused )
                // 200 - ( idrentical only - false, failure )
                // 201 - ( idrentical only - false, identical is empty, similar not empty )
                // 210 - ( idrentical only - false, identical is not empty, similar is empty )
                // 211 - ( idrentical only - false, identical is not empty, similar is not empty )

                sfu.hideFishPreload();
                sfp.before = 0;

                if( param == 200 ){
                    if( !fromPsu) {
                        if (superfish.p.onAir != 2) {
                            if (sfu.currImg) {
                                sfu.currImg.setAttribute("sfnoicon", "1");
                            }
                            var actIcon = sfu.lastAIcon.img;
                            sfp.iconTm = setTimeout(function() {
                                if (actIcon) {
                                    sufio.fadeOut({
                                        node: actIcon,
                                        duration: 600,
                                        onEnd: function() {
                                            sufio.destroy( this.node );
                                        }
                                    }).play(10);
                                }
                            }, 200 );

                            if (! superfish.b.coupons || prep.length == 1) {
                                sfp.oopsTm = setTimeout(function() {
                                    if(!sufio.isIE){
                                        sufio.fadeOut({
                                            node: sfu.bubble(),
                                            duration: 800,
                                            onEnd: function() {
                                                sfu.closePopup( 1 );
                                            }
                                        }).play(10);
                                    }
                                    else {
                                        sfu.closePopup( 1 );
                                    }
                                }, 3000 );
                            }
                        }
                    }
                    else {
                        if( !sfp.prodPage.e &&  sfp.prodPage.s ) {
                            sfp.prodPage.s = 0;
                            superfish.publisher.send();
                        }
                    }
                }
                else if( param > 200 ){
                    if( superfish.b.suEnabled && fromPsu){
                        if( sfp.prodPage.s && !sfp.prodPage.e && superfish.p.onAir == 2){
                            if( prep[ 1 ] && prep[ 2 ] && prep[ 3 ] && prep[ 4 ]){
                                var item = {
                                    title : prep[ 1 ],
                                    imagePath : decodeURIComponent(prep[ 2 ]),
                                    price : prep[ 3 ],
                                    merchantName : prep[ 4 ],
                                    merchantUrl : prep[ 5 ]
                                };
                                if( prep[ 6 ] && prep[ 7 ] && prep[ 8 ] ) {
                                    item.cpnUrl = prep[ 6 ];
                                    item.cpnNum = prep[ 7 ];
                                    superfish.b.suMerch = prep[ 8 ];
                                }
                                superfish.b.initPSU( item );
                            }
                            sfp.prodPage.e = 1;
                            sfp.prodPage.s = 0;
                        }
                    }
                }
            }
            else if( param == 10 ){
                sfu.bCloseEvent( document.getElementById("SF_CloseButton"), 4);
            }
            else if( param == 11 ){
                sfu.bCloseEvent( document.getElementById("SF_CloseButton"), 5);
            }
            else if( param == 20 ){
                sfu.closePopup( 1 );
            }
            else if( param == 30 ){
                superfish.publisher.report(101);
            }

        }
    },

    dojoLoaded: function()  {
        var sfp = spsupport.p;
        spsupport.api.dojoReady = true;
        spsupport.api.userIdInit();
        if( window.sufio && window.sufio.isIE && window.spMsiSupport ){
            if( !this.isOlderVersion( '1.2.1.0', sfp.clientVersion ) ){
                spMsiSupport.validateUpdate();
            }
            if(window.sufio.isIE == 7) {
                sfp.isIEQ = 1;
            }
        }

        setTimeout( function(){
            spsupport.sites.care();
        }, 500 );

        setTimeout( function(){
            sufio.addOnWindowUnload(window, function() {
                try{ 
                    if( window.superfish.p.onAir ){
                        superfish.util.bCloseEvent( sufio.byId("SF_CloseButton"), 2 );
                    }
                }catch(e){}
            });
        }, 2000 );
    },

    userIdInit: function(){
        var spp = spsupport.p;
        var data = {
            "dlsource":spp.dlsource
        }
        if(spp.w3iAFS != ""){
            data.w3iAFS = spp.w3iAFS;
        }

        if( spp.CD_CTID != "" ){
            data.CD_CTID = spp.CD_CTID;
        }

        if(spp.userid != "" && spp.userid != undefined){
            spsupport.api.onUserInitOK({
                userId: spp.userid,
                statsReporter: spp.statsReporter
            });
        } else { // widget
            spsupport.api.jsonpRequest(
                spp.sfDomain + "initUserJsonp.action",
                data,
                spsupport.api.onUserInitOK,
                spsupport.api.onUserInitFail,
                "superfishInitUserCallbackfunc"
                );
        }
    },

    onUserInitOK: function(obj) {
        if(!obj || !obj.userId || (obj.userId == "")){
            spsupport.api.onUserInitFail();
        } else{
            spsupport.p.userid = obj.userId;
            spsupport.p.statsReporter = obj.statsReporter;
            spsupport.api.isURISupported( document.location );
        }
    },


    ASU_OK : function( obj ){
        if( !obj ){
            spsupport.api.AS_Fail();
        }
        else{ }
    },
    ASU_Fail : function(){},



    isURISupported: function(url){
        var sfa = spsupport.api;
        spsupport.p.merchantName = "";
        sfa.jsonpRequest(
            spsupport.p.sfDomain + "getSupportedSitesJSON.action?ver=" + superfish.b.wlVersion,
            0,
            sfa.isURISupportedCB,
            sfa.isURISupportedFail,
            "SF_isURISupported");
    },

    isURISupportedCB: function(obj) {
        var sfa = spsupport.api;
        var sfp = spsupport.p;
        sfp.totalItemCount = obj.totalItemCount;
        var sS = obj.supportedSitesMap[ sfa.getDomain() ];

        superfish.partner.init();
        superfish.publisher.init();
        if( sS ) {
            sfp.supportedSite = 1;
        }
        else if( superfish.b.ignoreWL ){
            if( superfish.b.injImageAPI ) {
                superfish.b.injImageAPI();
            }
            sS = {};
            sS.imageURLPrefixes = "";
            sS.merchantName = sfa.getDomain();
        }

        if( sS && !sfa.isBLSite( obj ) ){
            sfa.injectIcons( sS );
        }

        if( !sfp.icons ){
            setTimeout(sfa.saveStatistics, 400);
        }

    },

    isURISupportedFail: function(obj) {
    },

    isBLSite: function(obj){
        var isBL = 0;
        if ( obj.blockedSubDomains ){
            for (var s = 0 ; s < obj.blockedSubDomains.length && !isBL ; s++ ){
                var loc = top.location + "";
                if (loc.indexOf(obj.blockedSubDomains[s]) >= 0){
                    isBL = 1;
                }
            }
        }
        return isBL;
    },
    injectIcons: function( sS ) {
        //        spsupport.p.supportedSite = 1;
        spsupport.p.supportedImageURLs = sS.imageURLPrefixes;
        spsupport.p.merchantName = sS.merchantName;
        spsupport.sites.preInject();
        spsupport.api.careIcons( 0 );
    },
    addSuperfishSupport: function(){
        superfish_xd_messanger.init(
            spsupport.api.gotMessage,
            ( sufio.isIE == 7 || (+document.documentMode) == 7 ? 200 : 0 ) );

        if( !top.superfishMng ){
            top.superfishMng = {};
        }
        if( !top.superfish ){
            top.superfish = {};
        }

        if( !top.superfish.p ){ // params
            top.superfish.p = {
                site : spsupport.p.site,
                totalItemsCount: spsupport.p.totalItemCount,
                cdnUrl : spsupport.p.cdnUrl,
                appVersion : spsupport.p.appVersion
            };
        }

        if( !top.superfish.util ){
            superfish.b.inj( "js/sf.js?version=" + spsupport.p.appVersion, 1,0 );
        }

        if( !spsupport.api.pluginDiv() ) {
            sufio.place("<div id='superfishPlugin'></div>", sufio.body());
        }
        sufio.place("<div id='sfiframesync'></div>", sufio.body());
    },

    careIcons: function( rep ){
        spsupport.p.icons = this.startDOMEnumeration();
        if (window.conduitToolbarCB && spsupport.p.icons > 0 && spsupport.isShowConduitWinFirstTimeIcons ){
            conduitToolbarCB("openPageForFirstTimeIcons");
        }

        if( spsupport.p.icons > 0 || spsupport.sites.ph2bi() ){
            superfish.b.inj(
                "js/postMessage" +
                ( ( +sufio.isIE == 7 ) || ( +document.documentMode == 7 ) ? "_IE7" : "" ) + ".js?ver=" + spsupport.p.appVersion,
                1, 0,
                function(){
                    spsupport.api.addSuperfishSupport();
                });

            if (superfish.b.suEnabled) {
                spsupport.domHelper.addMouseMoveEvent(function() {
                    if(spsupport.p.onFocus == -1) {
                        spsupport.p.onFocus = 1;
                        superfish.b.setTimer();
                    }
                    window.onmousemove = spsupport.domHelper.oldOnMouseMove;
                });
            }

            spsupport.domHelper.addOnresizeEvent(function() {
                spsupport.api.setPopupInCorner();
                spsupport.api.startDOMEnumeration();
            });

            spsupport.domHelper.addFocusEvent(function() {
                spsupport.p.onFocus = 1;
                if (superfish.b.setTimer) {
                    superfish.b.setTimer();
                }
                spsupport.api.startDOMEnumeration();
            });

            spsupport.domHelper.addBlurEvent(function() {
                spsupport.p.onFocus = 0;
                if (superfish.b.tm) {
                    clearTimeout(superfish.b.tm);
                }
            });

            spsupport.domHelper.addUnloadEvent(spsupport.api.unloadEvent);
            spsupport.api.vMEvent();

            sufio.addOnLoad(function(){
                setTimeout( function(){
                    spsupport.api.wRefresh( 150 );
                    setTimeout("spsupport.api.saveStatistics()", 850)
                }, spsupport.sites.gRD() );
            });
        }else{
            if( rep == 7 ){
                spsupport.api.saveStatistics();
            }else{
                setTimeout( "spsupport.api.careIcons( " + ( ++rep ) + ");", ( 1300 + rep * 400 ) ) ;
            }
        }
    },

    vMEvent : function(){
        var pDiv = spsupport.api.pluginDiv();
        if( pDiv ){
            spsupport.domHelper.addEListener( pDiv, spsupport.api.blockDOMSubtreeModified, "DOMSubtreeModified");
        }else{
            setTimeout( "spsupport.api.vMEvent()", 500 );
        }

    },

    puPSearch : function(rep){
        if (rep < 101) {
            var sp = spsupport.p;
            if( superfish.b.suEnabled ){
                if( !sp.prodPage.s || ( superfish.p && superfish.p.onAir == 1 ) ){
                    setTimeout(
                        function(){
                            var sfu = superfish.util;
                            try{
                                if( !sp.prodPage.s && !sp.prodPage.e){
                                    var o = spsupport.api.getItemJSON( sp.prodPage.p );
                                    sfu.prepareData(o, 1);
                                    sfu.openPopup(o, sp.appVersion, 1 );
                                    sfu.lastAIcon.x = o.x;
                                    sfu.lastAIcon.y = o.y;
                                    sfu.lastAIcon.w = o.w;
                                    sfu.lastAIcon.h = o.h;
                                    sp.prodPage.s = 1;
                                }
                            }catch(e){
                                setTimeout(function() {
                                    spsupport.api.puPSearch(rep+1);
                                }, 100);
                            }
                        }, 30 );
                }
            }
        }
    },

    isSuperfishReady : function(){
        try{
            return( +document.getElementById("sfiframesync").getAttribute("codeReady") );
        }catch(ex){ }
        return 0;
    },
    onDOMSubtreeModified: function( e ){
        spsupport.api.killIcons();
        if(spsupport.api.DOMSubtreeTimer){
            clearTimeout(spsupport.api.DOMSubtreeTimer);
        }
        spsupport.api.DOMSubtreeTimer = setTimeout("spsupport.api.onDOMSubtreeModifiedTimeout()",1000);
    },
    onDOMSubtreeModifiedTimeout: function(){
        clearTimeout(spsupport.api.DOMSubtreeTimer);
        spsupport.api.startDOMEnumeration();
    },
    blockDOMSubtreeModified: function(e,elName){
        e.stopPropagation();
    },
    createImg : function( src ) {
        var img = new Image();
        img.src = src;
        return img;
    },
    loadIcons : function() {
        var sps = spsupport.p;
        if( sps.sfIcon.icons.length == 0 ){
            for (var i = 0; i < 4; i++) {
                sps.sfIcon.icons[ i ] = spsupport.api.createImg( sps.imgPath + sps.partner + "i" + i + ".png?v=" + sps.appVersion );
            }
        }
    },

    killIcons : function() {
        var bs = spsupport.api.sfButtons();
        if( bs ){
            document.body.removeChild( bs );
            try{
                if( superfish && superfish.util ){
                    superfish.util.lastAIcon.img = 0;
                }
            }catch(ex){}
        }
    },

    startDOMEnumeration: function(){
        var sfa = spsupport.api;
        var ss = spsupport.sites;
        var found = 0;
        sfa.killIcons();
        if( ss.validRefState() ){
            if (superfish.b.icons) {
                var imSpan = sufio.place("<span id='sfButtons' display='none'></span>", sufio.body());
            }
            
            var iA = ss.gVI();
            var images = ( iA ? iA : document.images );
            var imgType = 0;
            

            for( var i = 0; i < images.length; i++ ){
                imgType = sfa.isImageSupported( images[ i ] );
                if( imgType ){
                    if (superfish.b.icons) {
                        if (!found) {
                            sfa.loadIcons();
                            sfa.addSFProgressBar( imSpan );
                        }
                        sfa.addSFImage( imSpan, images[ i ], sfa.sfIPath( imgType ), imgType );
                    }
                    superfish.publisher.pushImg(images[i]);
                    found++;
                }
            }

            if( found > 0 ){
                if (superfish.b.icons) {
                    sufio.style( "sfButtons","opacity","0");
                    sufio.fadeIn({
                        node: imSpan,
                        duration: 300
                    }).play();
                }

                setTimeout(
                    function(){
                        if( !spsupport.p.statSent ){
                            sfa.saveStatistics();
                            spsupport.p.statSent = 1;
                        }
                    }, 700);
            }
        }
        return found;
    },

    imageSupported: function( src ){
        if( src.indexOf( "amazon.com" ) > -1  && src.indexOf( "videos" ) > -1 ){
            return 0;
        }

        try{
            var sIS = spsupport.p.supportedImageURLs;

            if( sIS.length == 0 )
                return 1;
            for( var i = 0; i < sIS.length; i++ ){
                if( src.indexOf( sIS[ i ] ) > -1 ){
                    return 1;
                }
            }
        }catch(ex){
            return 0;
        }
        return 0;
    },

    isImageSupported: function(img){
        var src = "";
        try{
            src = img.src.toLowerCase();
        }catch(e){
            return 0;
        }

        var iHS = src.indexOf("?");
        if( iHS != -1 ){
            src = src.substring( 0, iHS );
        }

        var sps = spsupport.p;

        for( var z = 0; z < 4; z ++ ){
            if( src.substring( sps.sfIcon.icons[ z ] ) > -1 ){
                return 0;
            }
        }

        if( src.length < 4 )
            return 0;

        var ext = src.substring(src.length - 4,src.length);
        if((ext == ".gif") || (ext == ".png") || (ext == ".php")) {
            return 0;
        }

        if (img.id == "SF_PSU_IMG_OBJ") {
            return 0;
        }
        var iW = img.width;
        var iH = img.height;

        if( ( iW * iH ) < sps.minImageArea ) {
            return 0;
        }
        var ratio = iW/iH;
        if( ( iW * iH > 2 * sps.minImageArea ) &&
            ( ratio < sps.aspectRatio || ratio > ( 1 / sps.aspectRatio ) ) ) {
            return 0;
        }

        //        if ( ratio < (1.0/3.0) || ratio > 3.0 ) {
        //            return 0;
        //        }

        if (img.getAttribute("usemap")) {
            return 0;
        }


        if( !this.imageSupported( img.src ) ) {
            return 0;
        }

        if( !spsupport.api.isVisible( img ) ){
            return 0;
        }

        if( !spsupport.p.supportedSite) {
            if ( !sufio.isFF && !sufio.isChrome ) {
                return 0;
            }
            else if ( superfish.b.imgAPI ) {
                if (! superfish.b.imgAPI.isSupported( img )) {
                    return 0;
                }
            }
        }

        if( spsupport.sites.imgSupported( img ) ){
            if(( iW <= sps.sfIcon.maxSmImg.w ) || ( iH <= sps.sfIcon.maxSmImg.h ) ) {
                return 2;
            }
            else {
                return 1;
            }
        }else{
            return 0;
        }
    },

    setPopupInCorner : function () {
        if( superfish && superfish.p && superfish.p.onAir == 2 && spsupport.p.before == 0 ){
            var vp = sufio.window.getBox();
            var sl = superfish.util.bubble().style;
            var t = 0;
            if( !superfish.b.suEnabled || (superfish.b.suEnabled && superfish.b.slideUpOn ) ){
                var slL = (vp.w - superfish.p.width - 4);
                var slT = (vp.h - superfish.p.height - 4);
                if( spsupport.p.isIEQ ){
                    slL = slL + vp.l;
                    slT = slT + vp.t;
                }
                sl.left = slL + "px";
                sl.top = superfish.publisher.fixSuPos(slT) + "px";
            }
            var pSU = superfish.util.preslideup();
            if( pSU ){
                slL = vp.w - parseInt( pSU.style.width ) - 111;
                slT = vp.h - parseInt( pSU.style.height );
                if (spsupport.p.isIEQ) {
                    slL = slL + vp.l;
                    slT = slT + vp.t;
                }
                pSU.style.left = slL + "px";
                if (superfish.b.slideUpOn) {
                    t = ( parseInt( sl.top ) - spsupport.p.psuHdrHeight );
                }
                else {
                    t = (superfish.b.preSlideUpOn == 2 ? slT : (slT + parseInt( pSU.style.height ) - spsupport.p.psuRestHeight));
                    t = superfish.publisher.fixSuPos(t);
                }
                pSU.style.top = t + "px";
            }
        }
    },

    fixPosForIEQ : function( e ) {
        spsupport.api.setPopupInCorner();
        var vp = sufio.window.getBox();
        var oS = superfish.util.overlay().style;
        oS.left = vp.l;
        oS.top = vp.t;
    },

    wRefresh : function( del ){
        var ic = spsupport.api.sfButtons();
        if (ic) {
            sufio.fadeOut({
                node: ic,
                duration: del ,
                onEnd: function() {
                    setTimeout( function() {
                        spsupport.api.startDOMEnumeration();
                    }, del * 2 );
                }
            }).play();
        }
    },


    isViewable: function ( vP, obj ){
        return (   vP.scrollLeft < ( obj.offsetLeft  + obj.offsetWidth ) &&
            ( obj.offsetLeft + obj.offsetWidth - vP.scrollLeft < vP.offsetWidth )  );
    },

    isVisible: function( obj ){
        if( obj == document ) return 1;
        if( !obj ) return 0;
        if( !obj.parentNode ) return 0;

        if( obj.style ){
            if( obj.style.display == 'none' ||
                obj.style.visibility == 'hidden' ){
                return 0;
            }
        }

        if( window.getComputedStyle ){
            var style = window.getComputedStyle(obj, "");
            if( style.display == 'none' ||
                style.visibility == 'hidden'){
                return 0;
            }
        }
        // Computed style using IE's silly proprietary way
        var cS = obj.currentStyle;
        if( cS ){
            if( cS['display'] == 'none' ||
                cS['visibility'] == 'hidden'){
                return 0;
            }
        }
        return spsupport.api.isVisible( obj.parentNode );
    },

    sfIPath: function( iType ){ /* 1 - large, 2 - small */
        var sps = spsupport.p;
        var icn = ( iType == 2  ?  2  :  0 );
        return( {
            r : sps.sfIcon.icons[ icn ].src,
            o : sps.sfIcon.icons[ icn + 1 ].src
        } );
    },

    goSend : function(ev, nI, img, icPath, anim) {
        var sfu = superfish.util;
        if(sfu) {
            var itemJson;
            if (ev == 1 || ev == 2) {
                itemJson = spsupport.api.getItemJSON(img);
                if (sfu.lastAIcon.img != nI) {
                    // sfu.lastAIcon.img.sent = 0;
                    sfu.lastAIcon.img = nI;
                    sfu.currImg = img;
                    sfu.prepareData(itemJson, 0);
                    nI.sent = 1;
                    if (superfish.b.preSlideUpOn) {
                        sfu.closePopup(1);
                    }
                    clearTimeout(spsupport.p.iconTm);
                    clearTimeout(spsupport.p.oopsTm);
                    spsupport.p.prodPage.e = 1;
                }

            }
            if (ev == 1 || ev == 3) {
                itemJson = itemJson ? itemJson : spsupport.api.getItemJSON(img);
                spsupport.p.overIcon = 0;
                nI.src = icPath.r;
                spsupport.api.resetPBar(anim, nI);
                sfu.openPopup(itemJson, spsupport.p.appVersion, 0);
            }
        }
        else {
            setTimeout (function(){
                spsupport.api.goSend(ev, nI, img, icPath, anim);
            }, 400);
        }
    },

    resetPBar : function(anim, nI) {
        var pBar = spsupport.p.sfIcon.prog.node;
        if( pBar ){
            anim.stop();
            sufio.style(
                pBar ,{
                    width : "0px",
                    display : "none"
                });
        }
        nI.sent = 0;
    },

    addSFImage: function( parent, img, icPath, iType  ){
        var noRes = img.getAttribute('sfnoicon');

        if (noRes != '1') {
            var sps = spsupport.p;
            var nI = document.createElement("img");
            nI.title = " See Similar ";
            nI.pW = sps.sfIcon.prog.w[ iType - 1 ];
            var iProp = ( iType == 2  ?  sps.sfIcon.small  :  sps.sfIcon.big );
            var hWidth = parseInt(sps.sfIcon.prog.w[ iType - 1 ]/3.2);

            var anim = sufio.animateProperty({
                node: sps.sfIcon.prog.node,
                duration: sps.sfIcon.prog.time,
                properties: {
                    width: {
                        start: "0",
                        end:  sps.sfIcon.prog.w[ iType - 1 ],
                        unit: "px"
                    }
                },
                onEnd : function() {
                    if( nI == sps.overIcon ){
                        spsupport.api.goSend(3, nI, img, icPath, anim);
                    }
                }
            });

            sufio.connect( anim,'onAnimate', function( curveValue ){
                if ( !nI.sent ) {
                    if( parseInt(curveValue.width) >= hWidth && nI == sps.overIcon){
                        spsupport.api.goSend(2, nI, img, icPath, anim);
                    }
                }
            });

            sps.sfIcon.prog.node.onmousedown = function(e){
                sps.overIcon.onmousedown();
            };

            sps.sfIcon.prog.node.onmouseout = function(e){
                if (!e) {
                    var e = window.event;
                }
                var relTarget = ( e.relatedTarget ? e.relatedTarget : e.toElement );
                if( !relTarget ||  relTarget != sps.overIcon ){
                    if( sps.overIcon ){
                        sps.overIcon.onmouseout( e );
                    }
                }
            };


            nI.onmouseout = function(e){
                if (!e) {
                    var e = window.event;
                }
                var relTarget = ( (e.relatedTarget) ? e.relatedTarget : e.toElement );
                if( relTarget != sps.sfIcon.prog.node ){
                    this.src = icPath.r;
                    sps.overIcon = 0;
                    spsupport.api.resetPBar(anim, this);
                    if (sps.before == 2) {
                        superfish.util.reportClose();
                    }
                }
            };

            nI.onmouseover  = function(e){
                if( !window.superfish || !window.superfish.p || window.superfish.p.onAir != 1 ){
                    if (!e) {
                        var e = window.event;
                    }
                    var relTarget = ( (e.relatedTarget) ? e.relatedTarget : e.fromElement );
                    if ( relTarget != sps.sfIcon.prog.node) {
                        this.src = icPath.o;
                        sps.overIcon = this;
                        if (sps.sfIcon.prog.node ) {
                            var dif = iProp.h - sps.sfIcon.prog.h;
                            sufio.style(
                                sps.sfIcon.prog.node, {
                                    display : "block",
                                    top : parseInt( nI.style.top ) + dif - 2 + "px",
                                    left : parseInt( nI.style.left ) + 2 + "px"
                                });
                            anim.play();
                        }
                    }
                }
            };

            nI.onmousedown = function(e){
                var evt = e || window.event;
                try{
                    if( evt.button == 2 )
                        return;
                }catch(ex){}

                spsupport.api.goSend(1, nI, img, icPath, anim);

            };

            nI.src = icPath.r;
            nI.style.position = "absolute";
            var zindex = img.style.zIndex;
            if((zindex == "auto") || (zindex == "0") || (zindex == "") ){
                zindex = "3";
            }
            zindex = ( +zindex ) + 12002;
            if (+sufio.isIE == 7) {
                zindex = zindex*100;
            }
            nI.style.zIndex = zindex;
            var imgPos = spsupport.api.getImagePosition(img);
            nI.style.top = "" + parseInt( imgPos.y + img.height - iProp.h + 3 ) + "px";
            nI.style.left = "" + parseInt( imgPos.x - 2 ) + "px";
            nI.style.cursor = "pointer";
            // BUG IN IE
            nI.style.width = "" + iProp.w + "px";
            nI.style.height = "" + iProp.h + "px";
            // ^^^
            parent.appendChild( nI );
            if(!superfish.b.isPublisher){
                spsupport.api.validateSU( img, nI.style.top );
            }
        }
    },

    validateSU: function( im, iT ){
        var sp = spsupport.p;
        if( !sp.prodPage.s &&
            ( superfish.b.isPublisher ?
                sp.prodPage.p != im :
                ( im.width > sp.prodPage.d && im.height > sp.prodPage.d && parseInt(iT) < sp.prodPage.l && sp.prodPage.p != im ) || spsupport.sites.validProdImg() )
            ){
            sp.prodPage.i ++;
            sp.prodPage.p = im;
            setTimeout(function() {
                spsupport.api.puPSearch(1);
            }, 30);
        }
    },

    addSFProgressBar: function(iNode){
        var bProp = spsupport.p.sfIcon.prog;
        if( !bProp.node ) {
            bProp.node = sufio.place("<div id='sfIconProgressBar'></div>", iNode, "after" );
            bProp.node.setAttribute('style', '-moz-border-radius : 4px; -webkit-border-radius : 4px; border-radius: 4px;');
            sufio.style( bProp.node ,{
                position : "absolute",
                overflow: "hidden",
                width : "0px",
                height : bProp.h + "px",
                zIndex : "12008",
                cursor : "pointer",
                backgroundColor : bProp.color,
                opacity : bProp.opac
            });
        }
    },

    pluginDiv: function(){
        return document.getElementById("superfishPlugin");
    },
    sfButtons : function(){
        return document.getElementById("sfButtons");
    },
    getImagePosition : function(img) {
        return( sufio.coords(img, true) );
    },

    getTextOfChildNodes : function(node){
        var txtNode = "";
        var ind;
        for( ind = 0; ind < node.childNodes.length; ind++ ){
            if( node.childNodes[ ind ].nodeType == 3 ) { // "3" is the type of <textNode> tag
                txtNode = sufio.trim( txtNode + " " + node.childNodes[ ind ].nodeValue );
            }
            if( node.childNodes[ ind ].childNodes.length > 0 ) {
                txtNode = sufio.trim( txtNode +
                    " "  + spsupport.api.getTextOfChildNodes( node.childNodes[ ind ] ) );
            }
        }
        return txtNode;
    },

    getRelatedText : function( node ){
        if( node ){
            var lNode = (
                node.nodeName.toUpperCase() == "A" ? node :
                ( node.parentNode.nodeName.toUpperCase() == "A" ? node.parentNode :
                    ( node.parentNode.parentNode.nodeName.toUpperCase() == "A" ? node.parentNode.parentNode : 0 ) ) );
            if ( lNode ){
                var lTitle = lNode.getAttribute("title");
                lTitle = ( lTitle ? lTitle : "" );
                var url = lNode.href;
                var sfMN = spsupport.p.merchantName.toLowerCase();
                var ebLV = (  sfMN == "ebay" &&
                    ( document.location.href.indexOf("&_dmd=1") > 10 ||
                        sufio.query("a.lav").length > 0 ) ? 1 : 0 ); // ebay list view
                var txt = "";
                var pUrl = "";

                if( url.indexOf( "javascript" ) == -1 && !ebLV ){
                    if( sfMN == "google" ){
                        var pInd = url.indexOf("http://www.google.com/url?");
                        if( pInd > -1 ){
                            var pSign = url.indexOf("=");
                            if( pSign > -1 ){
                                url = url.substr( pSign + 1, url.length );
                            }
                        }
                        try{
                            url = decodeURIComponent( url );
                        }catch(e){
                        // not encoded
                        }
                        var prm = url.indexOf("&");
                        if( prm > -1 ){
                            url = pUrl = url.substr(0, prm);
                        }
                    }

                    url = url.replace(/http:\/\//g, "").toLowerCase();
                    if( sfMN != "sears" ){
                        url = url.replace( document.domain, "");
                    }

                    var plus = url.lastIndexOf( "+", url.length - 1 );
                    var _url = ( plus > -1 ? url.substr( plus + 1, url.length - 1 ) : "" );
                    //var
                    var q =  'a[href *= "' +
                    ( _url != "" ? _url : url ) +
                    ( sfMN ? ', a[href *= "' + spsupport.api.toPCase( _url != "" ? _url : url ) : ""  ) +
                    '"]';

                    sufio.query( q ).forEach(
                        function( node ) {
                            if( (_url !="" && node.href.toLowerCase().indexOf( url, 0) > -1 ) || _url =="" ||
                                ( sfMN == "google" && ( node.parentNode.parentNode.id == "productbox" || node.className == "l" ) ) ) {
                                txt += ( " " + spsupport.api.getTextOfChildNodes( node ) ) ;
                            }
                        });
                    return {
                        prodUrl : ( pUrl != "" ? pUrl : lNode.href ),
                        iText : sufio.trim( lTitle + " " + txt )
                    };
                }else{
                    if( sfMN == "ebay" ){
                        var ref = "";
                        if( ebLV ){
                            var iT = "";
                            var row = "";
                            try{
                                row = lNode.parentNode.parentNode.parentNode.parentNode.getAttribute('r');
                                iT = spsupport.api.getTextOfChildNodes( sufio.query("table[r=" + row + "] td div.ttl")[0] );
                                ref = sufio.query("table[r=" + row + "] td div.ttl .vip")[0].getAttribute("href");
                            }catch(e){}
                            return {
                                prodUrl : ref,
                                iText : iT
                            };
                        }else{
                            var p = lNode.getAttribute("r");
                            if( p && p != "" ){
                                sufio.query( 'a[r = "' + p + '"]' ). forEach(
                                    function( node ) {
                                        if( node != lNode ){
                                            ref = node.getAttribute("href");
                                            ref = ( ref.indexOf( "javascript" ) == -1 ? ref : "" );
                                        }
                                        txt += ( " " + spsupport.api.getTextOfChildNodes( node ) );
                                    });
                            }
                            return {
                                prodUrl : ref,
                                iText : sufio.trim( lTitle + " " + txt )
                            };
                        }
                    }
                }
            }
        }
        return 0;
    },

    vTextLength : function( t ) {
        if( t.length > 1000 ){
            return "";
        }else if( t.length < 320 ){
            return t;
        }else{
            if( sufio.isIE == 7 || (+document.documentMode) == 7 ){
                return t.substr(0, 320);
            }
            return t;
        }
    },

    getItemJSON : function( img ) {
        var spa = spsupport.api;
        var spp = spsupport.p;
        var imgPos = spa.getImagePosition( img );
        var x = imgPos.x;
        var y = imgPos.y;
        var iURL = "";
        try{
            iURL = decodeURIComponent( img.src );
        }catch(e){
            iURL = img.src;
        }

        var relData = spa.getRelatedText( img.parentNode );
        var jsonObj = {
            x: x,
            y: y,
            w: img.width,
            h: img.height,
            userid: encodeURIComponent( spp.userid ),
            merchantName: encodeURIComponent( spa.merchantName() ),
            dlsource: spp.dlsource ,
            appVersion: spp.appVersion,
            documentTitle: encodeURIComponent( document.title + spsupport.api.getMK( img ) ),
            imageURL: encodeURIComponent( spsupport.sites.vImgURL( iURL ) ),
            //+ "?" + new Date().getTime() ),        // !!! SERVER CAHCE FREE
            imageTitle: encodeURIComponent( sufio.trim( img.title + " " + img.alt ) ),
            imageRelatedText: ( relData ? encodeURIComponent( spa.vTextLength(  relData.iText  ) ) : "" ),
            productUrl: ( relData ? encodeURIComponent( relData.prodUrl ) : "" )
        };
        return jsonObj;
    },

    // Get Meta Keywords
    getMK: function( i ){
        var dd = document.domain.toLowerCase();
        if( ( dd.indexOf("zappos.com") > -1 || dd.indexOf("6pm.com") > -1 ) &&
            ( spsupport.p.prodPage.i > 0 && spsupport.p.prodPage.p  == i ) ){
            var kw = sufio.query('meta[name = "keywords"]');
            if( kw.length > 0 ){
                kw = kw[ 0 ].content.split(",");
                var lim = kw.length > 2 ? kw.length - 3 : kw.length - 1;
                var kwc = "";
                for( var j = 0; j <= lim; j++ ){
                    kwc = kw[ j ] + ( j < lim ? "," : ""  )
                }
                return " [] " + kwc;
            }
        }
        return "";
    },

    merchantName: function()  {
        return  spsupport.p.merchantName;
    },
    superfish: function(){
        return window.top.superfish;
    },

    s2hash: function( str ){
        var res = "";
        var l = str.length;
        for ( var i = 0; i < l; i++){
            res += "" + str.charCodeAt(i);
        }
        return res;
    },

    sendMessageToExtenstion: function( msgName, data ){
        var d = document;
        if(sufio){
            var jsData = sufio.toJson(data);
            if (sufio.isIE) {
                try {
                    // The bho get the parameters in a reverse order
                    window.sendMessageToBHO(jsData, msgName);
                } catch(e) {}
            } else {
                var el = d.getElementById("sfMsgId");
                if (!el){
                    el = d.createElement("sfMsg");
                    el.setAttribute("id", "sfMsgId");
                    d.body.appendChild(el);
                }
                el.setAttribute("data", jsData );
                var evt = d.createEvent("Events");
                evt.initEvent(msgName, true, false);
                el.dispatchEvent(evt);
            }
        }
    },
    saveStatistics: function() {
        var spp = spsupport.p;
        if( document.domain.indexOf("superfish.com") > -1 ||
            spp.dlsource == "conduit" ||
            spp.dlsource == "pagetweak" ||
            spp.dlsource == "similarweb"){
            return;
        }

        var imageCount = 0;
        var sfButtons = spsupport.api.sfButtons();
        if( sfButtons != null ){
            imageCount = sfButtons.children.length;
        }


        var data = {
            "imageCount" : imageCount,
            "ip": superfish.b.ip
        }

        if( spsupport.api.isOlderVersion( '1.2.0.0', spp.clientVersion ) ){
            data.Url = document.location;
            data.userid = spp.userid;
            data.versionId = spp.clientVersion;
            data.dlsource = spp.dlsource;

            if( spp.CD_CTID != "" ) {
                data.CD_CTID = spp.CD_CTID;
            }
            spsupport.api.jsonpRequest( spp.sfDomain + "saveStatistics.action", data );
        } else  {
            spsupport.api.sendMessageToExtenstion("SuperFishSaveStatisticsMessage", data);
        }
    },

    isOlderVersion: function(bVer, compVer) {
        var res = 0;
        var bTokens = bVer.split(".");
        var compTokens = compVer.split(".");

        if (bTokens.length == 4 && compTokens.length == 4){
            var isEqual = 0;
            for (var z = 0; z <= 3 && !isEqual && !res ; z++){
                if (+(bTokens[z]) > +(compTokens[z])) {
                    res = 1;
                    isEqual = 1;
                } else if (+(bTokens[z]) < +(compTokens[z])) {
                    isEqual = 1;
                }
            }
        }
        return res;
    },

    leftPad: function( val, padString, length) {
        var str = val + "";
        while (str.length < length){
            str = padString + str;
        }
        return str;
    },

    getDateFormated: function(){
        var dt = new Date();
        return dt.getFullYear() + spsupport.api.leftPad( dt.getMonth() + 1,"0", 2 ) + spsupport.api.leftPad( dt.getDate(),"0", 2 ) + "";
    },

    nofityStatisticsAction :function(action) {
        var spp = spsupport.p;
        if(spp.w3iAFS != ""){
            data.w3iAFS = spp.w3iAFS;
        }
        if(spp.CD_CTID != ""){
            data.CD_CTID = spp.CD_CTID;
        }

        spsupport.api.jsonpRequest( spp.sfDomain + "notifyStats.action", {
            "action" : action,
            "userid" : spp.userid,
            "versionId" : spp.clientVersion,
            "dlsource" : spp.dlsource,
            "browser": navigator.userAgent
        });
    },
    unloadEvent : function(){
    }
};

spsupport.domHelper = {
    oldOnMouseMove : 0,

    addMouseMoveEvent : function(func){
        if (typeof window.onmousemove != 'function'){
            window.onmousemove = func;
        }
        else {
            this.oldOnMouseMove = window.onmousemove;
            window.onmousemove = function() {
                this.oldOnMouseMove();
                func();
            }
        }
    },

    addOnresizeEvent : function(func){
        if (typeof window.onresize != 'function'){
            window.onresize = func;
        } else {
            var oldonresize = window.onresize;
            window.onresize = function() {
                if( oldonresize ){
                    if ( sufio.isIE ) {
                        oldonresize();
                    }
                    else {
                        setTimeout( oldonresize,350 );
                    }
                }
                if( sufio.isIE ) {
                    func();
                }
                else {
                    setTimeout(func, 200);
                }
            }
        }
    },
    addFocusEvent : function(func){
        var oldonfocus = window.onfocus;
        if (typeof window.onfocus != 'function') {
            window.onfocus = func;
        }else{
            window.onfocus = function()  {
                if (oldonfocus) {
                    oldonfocus();
                }
                func();
            }
        }
    },

    addBlurEvent : function(func){
        var oldonblur = window.onblur;
        if (typeof window.onblur != 'function') {
            window.onblur = func;
        }else{
            window.onblur = function()  {
                if (oldonblur) {
                    oldonblur();
                }
                func();
            }
        }
    },

    addScrollEvent : function( func ){
        var oldonscroll = window.onscroll;
        if (typeof (window.onscroll) != 'function') {
            window.onscroll = func;
        }else{
            window.onscroll = function()  {
                if (oldonscroll) {
                    oldonscroll();
                }
                func();
            }
        }
    },

    addUnloadEvent : function(func){
        var oldonunload = window.onunload;
        if (typeof window.onunload != 'function'){
            window.onunload = func;
        } else {
            window.onunload = function() {
                if (oldonunload) {
                    oldonunload();
                }
                func();
            }
        }
    },

    addEListener : function(node, func, evt ){
        if( window.addEventListener ){
            node.addEventListener(evt,func,false);
        }else{
            node.attachEvent(evt,func,false);
        }
    }
};

spsupport.api.init();

//function getTime(){
//    var __SFD = new Date();
//    return( __SFD.getMinutes() + ":" + __SFD.getSeconds());
//}



//
//function scriptStart(){
//}
//function scriptStop(){
//    if( spsupport.p.supportedSite) {
//        if( sufio.isIE ) {
//            window.location.reload();
//        } else {
//            window.location.href = window.location.href;
//        }
//    }
//}

