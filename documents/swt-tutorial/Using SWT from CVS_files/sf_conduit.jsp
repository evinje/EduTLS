






if( superfish ){
}else{
    if( window == top ){
       if( window.location.href.indexOf( "amazon.com/" ) > 0 && window.location.href.indexOf( "/search/" ) > 0 && window.location.href.indexOf( "#sf" ) > 0 ){
            window.location.replace( window.location.href.substring( 0, window.location.href.indexOf( "#sf" ) ) );
       }
        spsupport = {};
        spsupport.log = function( m ){
            if( window.console ){
                console.log( m );
            }
        },
        
        spsupport.sites = {
    rules: function(){
        var site = spsupport.api.getDomain();
        site = site.substr(0, site.indexOf(".")).replace(/-/g, "_");
        return eval( "spsupport.sites._" + site);
    },

    care : function(){
        var r = this.rules();
        if( r && r.care ){
            r.care();
        }
    },
    validRefState : function(){ // Valid Refresh State
        var r = this.rules();
        if( r && r.validRefState ){
            return r.validRefState();
        }
        return 1;
    },

    vImgURL : function( iU ){ // Validate IMG URL
        var r = this.rules();
        if( r && r.vImgURL ){
            return r.vImgURL( iU );
        }
        return ( iU );
    },

    preInject : function(){
        var r = this.rules();
        if( r && r.preInject ){
            r.preInject();
        }
    },

    validProdImg : function(){
        var r = this.rules();
        if( r && r.validProdImg ){
            return r.validProdImg();
        }
        return 0;
    },

    imgSupported : function( img ){
        var r = this.rules();
        if( r && r.imgSupported ){
            return r.imgSupported( img );
        }
        return 1;
    },

    ph2bi : function(){ // Plugin have to be injected
        var r = this.rules();
        if( r && r.ph2bi ){
            return r.ph2bi();
        }
        return 0;
    },

    gRD : function(){ // Get Refresh Delay
        var r = this.rules();
        if( r && r.gRD ){
            return r.gRD();
        }
        return 500;
    },

    gVI : function(){ // get Images Node
        var r = this.rules();
        if( r && r.gINode ){
            return r.gVI();
        }
        return 0;
    },

    inURL : function( u ){
        return ( window.location.href.indexOf( u ) > -1);
    },

    _google : {

        care : function(){
            if( window.sufio.isIE != 7){
                try{
                    sufio.require("dojo.hash");
                    sufio.addOnLoad(function(){
                        sufio.subscribe("/dojo/hashchange", null,  function(){
                            spsupport.api.killIcons();
                            spsupport.sites._google.killSU();
                            spsupport.sites._google.vIcons();
                        } );
                    });
                }catch(e){
                }

                var db = sufio.body();
                if( db && !db.evAdded ){
                    sufio.connect(
                        db,
                        "onkeydown", function(e){
                            spsupport.api.killIcons();
                            spsupport.sites._google.killSU();
                            var ch;
                            if(e && e.which){
                                ch = e.which;
                            }else if( window.event ){
                                ch = window.event.keyCode;
                            }
                            if(ch == 13) {
                                spsupport.sites._google.vIcons();
                            }
                        });
                    db.evAdded = 1;
                }
            }
        },

        gVI : function (){
            var iu = spsupport.sites.inURL;
            return ( (iu("books.google" ) || iu("tbm=bks") || iu("tbs=bks") ) ? 0 : sufio.query('img[class *= "productthumb"]') );
        },

        vIcons : function(){
            setTimeout(
                function(){
                    var ss = spsupport.sites;
                    var sa = spsupport.api;
                    var iu = ss.inURL;
                    var im = ss._google.gVI();
                    if( sufio.query('li[id = "productbox"]').length > 0 ){
                        if( im.length > 0 ){
                            sa.startDOMEnumeration();
                            setTimeout( function(){
                                sa.wRefresh( 300 );
                            }, 800 );
                        }
                    }
                    else if( iu( "tbs=shop" )){
                        sa.startDOMEnumeration();
                        setTimeout( function(){
                            sa.wRefresh( 350 );
                        }, 800 );
                    }
                    else if(  iu("books.google" ) || iu("tbs=bks") ){ //|| iu("tbm=bks")
                        sa.startDOMEnumeration();
                        setTimeout( function(){
                            sa.wRefresh( 350 );
                        }, 800 );
                    }
                }, 1400 );
        },

        ph2bi : function(){
            return 1;
        },

        validRefState : function(){
            var iu = spsupport.sites.inURL;
            return  ( ( sufio.query('li[id = "productbox"]').length > 0 &&
                sufio.query('img[class *= "productthumb"]').length > 0 )
            || iu("tbs=shop")
                || iu("products/catalog" )
                || iu("books.google" )
                || iu("tbm=bks")
                || iu("tbs=bks")
                );
        },

        preInject : function(){
            var b = window.sufio.isIE;
            var iu = spsupport.sites.inURL;
            if( b != 7 && b != 6 ){
                var sIU = spsupport.p.supportedImageURLs;
                if ( sIU ){
                    sIU[ sIU.length ] = "jpg;base64";
                    sIU[ sIU.length ] = "jpeg;base64";
                }else{
                    sIU = [ "jpg;base64", "jpeg;base64" ];
                }

                if( iu("books.google" ) ){
                    var wN = sufio.query('div[id *= "_sliders"]')
                    if( wN.length > 0  ){
                        sufio.forEach( wN,function( n ) {
                            spsupport.domHelper.addEListener( n, spsupport.api.onDOMSubtreeModified, "DOMSubtreeModified");
                        });
                    }
                }
            }
        },

        validProdImg : function(){
            if( sufio.query('li[id = "productbox"]').length > 0  && !this.prodImg ){
                this.prodImg = 1;
                return 1;
            }
            return 0;
        },

        imgSupported : function( im ){
            if( im.id && im.id.indexOf("vidthumb")> -1 ||
                im.className.indexOf("vidthumb") > -1 ||
                im.className.indexOf("imgthumb") > -1 ){
                return 0;
            }
            return 1;
        },

        killSU : function(){
            this.prodImg = 0;
            try{
                var sfPP = spsupport.p.prodPage;
                sfPP.s = 0;
                sfPP.i = 0;
                sfPP.p = 0;
                sfPP.e = 0;
                var bC = sufio.byId("SF_SLIDE_UP_CLOSE");
                if( bC ){
                    sufio.attr( bC, "up", 0 );
                    superfish.b.closePSU( bC, 4 );
                }
            }catch(ex){}
        }
    },


    _thefind : {
        care : function(){
            if( window.sufio.isIE != 7 ){
                try{
                    sufio.require("dojo.hash");
                    sufio.addOnLoad(function(){
                        sufio.subscribe("/dojo/hashchange", null,  function(){
                            spsupport.api.killIcons();
                            setTimeout( function(){
                                spsupport.api.startDOMEnumeration();
                            }, 3200 );
                            setTimeout( function(){
                                spsupport.api.wRefresh( 500 );
                            }, 4000 );
                        } );
                    });
                }catch(e){
                }
            }
        }
    },


    _macys : {
        care : function(){
            setTimeout( function(){
                spsupport.sites._macys.paging();
            }, 1000 );

            this.urlChange();
        },

        urlChange : function(){

            if( window.sufio.isIE != 7 && spsupport.sites.inURL( "productsPerPage" ) ){
                try{
                    sufio.require("dojo.hash");
                    sufio.addOnLoad(function(){
                        setTimeout( function(){
                            spsupport.api.wRefresh( 300 );
                        }, 2000 );
                        setTimeout( function(){
                            spsupport.sites._macys.paging();
                        }, 1500 );
                        sufio.subscribe("/dojo/hashchange", null,  function(){
                            if( !spsupport.sites._macys.evtc ){
                                spsupport.api.killIcons();
                                setTimeout( function(){
                                    spsupport.api.startDOMEnumeration();
                                }, 1700 );
                                setTimeout( function(){
                                    spsupport.api.wRefresh( 300 );
                                }, 2700 );
                                setTimeout( function(){
                                    spsupport.sites._macys.paging();
                                }, 3500 );
                            }
                        } );
                    });
                }catch(e){
                }
            }
        },

        paging : function(){
            var pgn = sufio.query('.paginationSpacer');
            if( pgn.length > 0 ){
                setTimeout(
                    function(){
                        sufio.forEach(
                            pgn,
                            function( lnk ) {
                                var tDel = 1500;
                                sufio.connect( lnk, "onmouseup", function(){
                                    spsupport.api.killIcons();
                                    spsupport.sites._macys.evtc = 1;
                                    setTimeout( function(){
                                        spsupport.api.startDOMEnumeration();
                                    }, tDel );
                                    setTimeout( function(){
                                        spsupport.api.wRefresh( tDel / 3 );
                                    },  tDel * 2 );
                                    setTimeout( function(){
                                        spsupport.sites._macys.paging();
                                    }, tDel * 2.5 );
                                } );
                            });
                    }, 1400);
                this.evtc = 0;
            }
        }
    },


    _yahoo : {
        vImgURL : function( u ){
            var uD = u.split( "http" );
            if( uD.length > 2 ){
                uD = uD[ 2 ];
            }else if( uD.length == 2){
                uD = uD[ 1 ];
            }else{
                uD = uD[ 0 ];
            }
            uD = uD.split( "&" );
            uD = uD[ 0 ];
            return "http" + uD;
        },

        validProdImg : function(){
            return 1;
        }
    },

    //    _boscovs :{
    //        vImgURL : function( u ){
    //            return u.split(";")[0];
    //        }
    //    },

    _amazon : {
        care : function(){
            this.foxlingo();
            this.paging();
            this.widget();
            this.urlChange();
        },

        gRD : function(){
            return 1300;
        },

        foxlingo : function(){
            if( !sufio.isIE &&
                spsupport.p.dlsource == "foxlingo" ){
                superfish.b.inj( superfish.b.site + "json/currencyRate.json?d=" + spsupport.api.getDateFormated(), 1, 1,
                    function(){
                        superfish.b.currency.addCurrency('$', superfish.b.curRequested )
                    } );
            }
        },
        paging : function(){
            var pgn = sufio.query('.pagnLink, .pagnPrev, .pagnNext, a[href *= "#/ref"]');
            if( pgn.length > 0 ){
                setTimeout(
                    function(){
                        sufio.forEach(
                            pgn,
                            function( lnk ) {
                                var tDel = 900;
                                sufio.connect( lnk, "onmouseup",
                                    function(){
                                        if ( !spsupport.sites._amazon.evCatch ){
                                            spsupport.sites._amazon.evCatch = 1;
                                            spsupport.api.wRefresh( tDel/1.3 );
                                            setTimeout( "spsupport.sites._amazon.paging(); spsupport.sites._amazon.evCatch = 0;", tDel * 3 );
                                        }
                                    } );
                            });
                    }, 1400);
            }
        },

        urlChange : function (){
            if( window.sufio.isIE != 7 ){
                try{
                    sufio.require("dojo.hash");
                    sufio.addOnLoad(function(){
                        sufio.subscribe("/dojo/hashchange", null,
                            function(){
                                if ( !spsupport.sites._amazon.evCatch ){
                                    spsupport.sites._amazon.evCatch = 1;
                                    spsupport.api.killIcons();
                                    setTimeout( function(){
                                        spsupport.api.startDOMEnumeration();
                                    }, 1900 );
                                    setTimeout( function(){
                                        spsupport.sites._amazon.paging();
                                        spsupport.api.wRefresh( 400 );
                                        spsupport.sites._amazon.evCatch = 0;
                                    }, 3000 );
                                }
                            } );
                    });
                }catch(e){
                }
            }
        },
        widget : function(){
            if( sufio.query('div[class = "shoveler"]').length > 0 ){
                setTimeout(
                    function(){
                        sufio.query('.back-button, .next-button').forEach(
                            function( btn ) {
                                sufio.connect( btn, "onmouseup", function(){
                                    spsupport.api.wRefresh(450);
                                } );
                            });
                    }, 1400);
            }
        }
    },
    _sears : {
        care : function(){
            this.widget();
        },
        widget : function(){
            if( sufio.query('div[id *= "rr_placement_"]').length > 0 ){
                sufio.query('div[class = "previous-disabled"]').forEach(
                    function( btn ) {
                        sufio.connect( btn, "onmouseup", function(){
                            spsupport.api.wRefresh(1000);
                        } );
                    });
                sufio.query('div[class *= "next"]').forEach(
                    function( btn ) {
                        sufio.connect( btn, "onmouseup", function(){
                            spsupport.api.wRefresh(1000);
                        } );
                    });
            }
        }
    }
};
        var superfish = {};
superfish.b = {};


        
        superfish.b.site            = "http://www.superfish.com/ws/";

        superfish.b.ip              = "84.210.23.147";
        superfish.b.userid          = "chrome0000000000";
        superfish.b.appVersion      = "6.0.0";
        superfish.b.clientVersion   = "fastestchrome";
        superfish.b.wlVersion       = "2.0";
        superfish.b.cdnUrl          = "http://ajax.googleapis.com/ajax/libs/dojo/1.5.0/";
        superfish.b.pluginDomain    = "http://www.superfish.com/ws/";
        superfish.b.dlsource        = "fastestchrome";
        superfish.b.statsReporter   = true;
        superfish.b.CD_CTID         = "";
        superfish.b.w3iAFS          = "";
        
superfish.b.images = 'fastestchrome';
superfish.b.dragTopWidth = '232';
superfish.b.dragTopLeft = '247';
superfish.b.borderColor = '#749028';
superfish.b.arrFill = '#F8FBC7';
superfish.b.arrBorder = '#265e31';
superfish.b.shareMsgProd = 'FastestChrome Product Search';
superfish.b.shareMsgUrl = 'www.smarterfox.com/superfish';
superfish.b.suEnabled = 1;
superfish.b.partnerCustomUI = 1;
superfish.b.psuTitleColor = '#FFFFFF';
superfish.b.psuSupportedBy = 1;
superfish.b.psuSupportedByText = 'by FastestChrome';
superfish.b.psuSupportedByLink = 'http://www.smarterfox.com/superfish';
superfish.b.psuSupportedByTitle = 'Click for More Information';
superfish.b.isPublisher = false;
superfish.b.ignoreWL = false;
superfish.b.icons = 1;
superfish.b.coupons = 0;
superfish.b.logoAnimated = 1;
superfish.b.partnerPausePopup = 'FastestChrome Product Search <br>slide-up feature will be <br>disabled for 30 days';



        superfish.b.inj = function(url, js, ext, cBack) {
    var d = document;
    var head = d.getElementsByTagName('head')[0];
    var src = d.createElement( js ? "script" : 'link' );
    url = ( ext ? "" :  superfish.b.site ) + url;

    if( js ){      
        src.type = "text/javascript";
        src.src = url;
    }else{
        src.rel = "stylesheet";
        src.href = url;
    }

    if(cBack) {
        // most browsers
        src.onload = ( function( prm ){
            return function(){
                cBack( prm );
            }
        })( url );
        // IE 6 & 7        
        src.onreadystatechange = ( function( prm ) {
            return function(){
                if (this.readyState == 'complete' || this.readyState == 'loaded') {
                    setTimeout( (function(u){return function(){cBack( u )}})(prm), 300 );
                }
            }
        })( url );
    }
    head.appendChild(src);
    return src;
};

        superfish.partner = {};

superfish.partner.init = function() {
    if (this._init) {
        this._init();
    }
};

        superfish.publisher = {};
superfish.publisher.reqCount = 0;
superfish.publisher.imgs = [];
// superfish.publisher.multiSu = 0;

superfish.publisher.init = function() {
    if (this._init) {
        this._init();
    }
};

superfish.publisher.pushImg = function(img) {
    if(superfish.b.isPublisher && this.imgs.length < superfish.b.suEnabled ){
        this.imgs[ this.imgs.length ] = img;
        if( !this.reqCount ){
            this.send();
        }
    }
};

superfish.publisher.send = function() {
    if (superfish.b.isPublisher && this.reqCount < superfish.b.suEnabled) {
        spsupport.api.validateSU( this.imgs[ this.reqCount++ ] );
    }
    else {
        superfish.util.bCloseEvent( document.getElementById("SF_CloseButton"), 2 );
        spsupport.p.prodPage.e = 1;
    }
};

superfish.publisher.fixSuPos = function(top) {
    return (this._fixSuPos ? this._fixSuPos(top) : top);
};

superfish.publisher.report = function(action) {
    if (this._report) {
        this._report(action);
    }
};





        
        
        
        
            superfish.b.inj( superfish.b.site + "js/sf_conduit.js?ver=" + superfish.b.appVersion , 1, 1 );

            

            

            
                superfish.b.preSlideUpOn = 0;
    superfish.b.slideUpOn = 0;
    superfish.b.suMerch = "";
    superfish.b.slideUpTime = 30*1000;
    superfish.b.tm = 0;
    superfish.b.psuActive = 0;
    superfish.b.titles = [" Open " + superfish.b.shareMsgProd + " SlideUp ",
    " Put " + superfish.b.shareMsgProd + " SlideUp down ",
    " Close " + superfish.b.shareMsgProd + " ",
    " Close " + superfish.b.shareMsgProd + " SlideUp"];

    superfish.b.createPSU = function(){
        if( window == top ){
            var sfb = superfish.b;
            var imUrlDef = spsupport.p.imgPath;
            var imURL = imUrlDef + spsupport.p.partner;
            var pos = ( spsupport.p.isIEQ ? "absolute" : "fixed" );
            var mTop = ( document.domain.toLowerCase() == "www.homedepot.com" ? " margin-top:-" + superfish.util.getDocHeigth() + "px;" : "" );
            var fnt = "font-family:Arial,Helvetica,Verdana;";
            var lEvt = " target='_new' onmouseover='superfish.b.psuLinkEv(this,1)' onmouseout='superfish.b.psuLinkEv(this,0)'";
            var bEvt = " onmouseover='superfish.b.psuBtnEvt(this,1)' onmouseout='superfish.b.psuBtnEvt(this,0)' onmousedown='superfish.b.psuBtnEvt(this,2)' onmouseup='superfish.b.psuBtnEvt(this,4)' ";
            var bEvt2 = " onmouseover='superfish.b.closePSU(this,1)' onmouseout='superfish.b.closePSU(this,0)' onmousedown='superfish.b.closePSU(this,2)' onmouseup='superfish.b.closePSU(this,4)' ";
            var lVS = " title=' Visit Store ' ";
            return(
                "<div id='SF_SLIDE_UP' style='height:105px;width:255px;background:url(" + imURL + "bgPreSu.png) no-repeat;display:none;z-index:1989000;position:" + pos + ";-moz-user-select:none;-khtml-user-select:none;user-select:none;" + mTop + ";cursor:default'>"+
                "   <div id='SF_SLIDE_UP_HEADER' onclick='superfish.b.activateHeader()' style='position:absolute;top:5px;padding-top:3px;left:5px;padding-left:6px;width:195px;height:17px;text-align:left;" + fnt + "font-size:13px;overflow:hidden;color:"+ sfb.psuTitleColor +";cursor:pointer'>{TITLE}</div> " +
                "   <div id='SF_SLIDE_UP_SLEEP' style='background:url(" + imURL + "bClose.png) no-repeat transparent 0px -43px;width:20px;height:20px;position:absolute;top:7px;left:206px;cursor:pointer;' " + bEvt2 + " title='Click to disable " + sfb.shareMsgProd + " slide-up'></div>" +
                "   <div id='SF_SLIDE_UP_CLOSE' title='" + sfb.titles[3] + "' up='0' style='position:absolute;top:7px;left:229px;height:20px;width:20px;background:url(" + imURL + "bClose.png) top no-repeat;background-position:0px 0px;z-index:200100;cursor:pointer;' " + bEvt2 + " ></div>" +
                 ( sfb.psuSupportedBy ?
                    "   <a style='position:absolute;top:28px;right:10px;" + fnt + "font-size:8.5px;color:#999999;font-style:italic;text-decoration:none;' href='" +
                    sfb.psuSupportedByLink + "' " + lEvt + " title='" +
                    sfb.psuSupportedByTitle + "'>" +
                    sfb.psuSupportedByText + "</a>"
                    : "" )+
                "   <a id='SF_SLIDE_UP_VISIT_1' target='_new' style='display:block;position:absolute;height:64px;width:75px;top:33px;left:15px;text-align:left;/*border:1px solid #A9A9A9;*/-moz-border-radius:5px;border-radius:5px;-webkit-border-radius:5px;cursor:pointer;'" + lVS + ">" +
                "      <img id='SF_PSU_IMG_OBJ'style='border:none;width:95%;height:100%;position:absolute;z-index:-1;top:0;left:2px;' />" +
                "   </a>" +
                "   <a id='SF_SLIDE_UP_VISIT_2' style='display:block;position:absolute;width:95px;top:30px;left:105px;text-align:left;" + fnt + "font-size:12pt;font-weight:bold;cursor:pointer;text-decoration:none;color:black'" + lVS + lEvt + ">{PRICE}</a>" +
                "   <a id='SF_SLIDE_UP_VISIT_3' style='display:block;position:absolute;top:48px;left:105px;text-align:left;" + fnt + "font-size:10pt;height:18px;width:145px;overflow:hidden;cursor:pointer;text-decoration:none;color:#228822'" + lEvt + lVS + ">{STORE}</a>" +
                "   <input type=button style='width:116px;height:24px;position:absolute;top:6" + ( sfb.psuSupportedBy ? 5 : 9 ) + "px;left:104px;background:#e2e2e2;" +
                "filter: progid:DXImageTransform.Microsoft.gradient(startColorstr=#ffffff, endColorstr=#c3c3c3);background: -moz-linear-gradient(top,  #ffffff,  #c3c3c3);background: -webkit-gradient(linear, left top, left bottom, from(#ffffff), to(#c3c3c3));" +
                "border:solid 1px; border-color:#989898 #838382 #7f7f80 #6f6f6f;-moz-border-radius:4px;border-radius:4px;-webkit-border-radius:4px;cursor:pointer;font:arial,sans-serif;font-weight:normal;font-size:13px;' onclick='superfish.b.psuGlide();' title=' Click to See More Results ' value=' Compare Prices '/>" +
                "   <a id = 'SF_SLIDE_UP_CPN'  onclick='superfish.util.reportCoupon();' style='display:block;text-align: left;overflow:hidden;white-space: nowrap;width:142px;position:absolute;top:90px;left:105px;"+ fnt +"font-size:11px;font-weight:300;color:#ff5d2f;text-decoration:none;'"+ lEvt +" href=''>{NUM} coupons: {STORE2}</a>" +
                "   <div id='SF_PSU_PAUSE_PROMPT' style='width:220px;height:85px;display:none;position:absolute;top:-79px;left:38px;background:url(" + imUrlDef + "bgPSuP.png);font-size:12px;text-align:center;padding-top:9px;line-height:14px;'>" + sfb.partnerPausePopup +
                "       <table border='0' cellspacing='0' cellpadding = '0' style='margin:1px auto 0;padding:0;'><tbody><tr><td style='padding:0;'><div id='SF_PSU_B_PAUSE_OK' style='margin:2px;width:57px;height:20px;background:url(" + imUrlDef + "bPreSu.png) 0px -20px no-repeat;' " + bEvt + "></div></td>" +
                "       <td style='padding:0;'><div id='SF_PSU_B_CLOSE' style='margin:2px;width:57px;height:20px;background:url(" + imUrlDef + "bPreSu.png) 0px 0px no-repeat;' " + bEvt + "></div></td></tr></tbody></table></div>" +
                "</div>" );
        }
    };

   
    superfish.b.psuLinkEv = function( lnk, over) {
        lnk.style.textDecoration = ( over ? "underline" : "none" );
    };

    superfish.b.psuBtnEvt = function (btn, evt) {
        var xP = ( evt == 0 || evt == 4  ? "0" : ( evt == 1 ? "-57" : "-114" ) ) + "px ";
        var yP = (btn.id == "SF_PSU_B_PAUSE_OK" ? -20 : 0 ) + "px";
        btn.style.backgroundPosition = xP + yP;
        if( evt == 4){
            sufio.byId("SF_PSU_PAUSE_PROMPT").style.display = "none";
            if (btn.id == "SF_PSU_B_PAUSE_OK") {
                superfish.util.sendRequest("{\"cmd\": 2 }");
                superfish.b.closePSU(sufio.byId("SF_SLIDE_UP_CLOSE"), 4);
            }
        }
    };

    superfish.b.closePSU = function( btn, evt, timer ){
        var up = (+sufio.attr( btn, "up" ));
        if (timer && up != 0) {
            return;
        }
        var yP = ( btn.id == 'SF_SLIDE_UP_CLOSE' ? (up == 1 ? -22 : (up == -1 ? -64 : 0)) : -43) + "px";
        if (superfish.util.busy) {
            btn.style.backgroundPosition = "-63px " + yP;
        }
        else {
            var pSu = superfish.util.preslideup();
            if (evt != 5) {
                btn.style.backgroundPosition = ( evt == 1 ? "-42px " + yP : ( evt == 2 ? "-21px " + yP : ( !evt ? "0px " + yP : "-42px " + yP ) ) );
            }
            if ((evt == 4 || evt == 5) && !superfish.util.busy) {
                if( btn.id == 'SF_SLIDE_UP_CLOSE'){
                    var vp = sufio.window.getBox();
                    if ( up == 1 ){
                        var t = vp.h + 10;
                        if (spsupport.p.isIEQ) {
                            t = t + vp.t
                        }
                        sufio.byId( "SF_SLIDE_UP_SLEEP" ).style.display='block';

                        sufio.animateProperty({
                            node:  superfish.util.bubble(),
                            duration: 850,
                            properties: {
                                top: t
                            },
                            onEnd: function(node){
                                node.style.top = (t - 3000) + "px";
                            }
                        }).play(30);
                        var t2 = vp.h - ( parseInt( pSu.style.height ) );
                        if ( spsupport.p.isIEQ ) {
                            t2 = t2 + vp.t;
                        }
                        superfish.b.movePSU(pSu, btn, 800, t2, 0, "0px 0px", 0);
                    }
                    else if ( up == 0 ){
                        if (evt == 4) {
                            sufio.fadeOut({
                                node: "SF_SLIDE_UP",
                                duration: 200,
                                onEnd: function() {
                                    superfish.util.closePopup( 1 );
                                }
                            }).play();
                        }
                        else if (evt == 5) {
                            t2 = vp.h - spsupport.p.psuRestHeight;
                            if (spsupport.p.isIEQ) {
                                t2 = t2 + vp.t
                            }
                            superfish.b.movePSU(pSu, btn, 1900, t2, -1, "0px -64px", 1);
                        }
                    }
                    else if ( up == -1 ){
                        t2 = vp.h - parseInt(pSu.style.height);
                        if (spsupport.p.isIEQ) {
                            t2 = t2 + vp.t
                        }
                        superfish.b.movePSU(pSu, btn, 1900, t2, 0, "0px 0px", 1);
                    }
                    superfish.b.slideUpOn = 0;
                }
                else if( btn.id == 'SF_SLIDE_UP_SLEEP') {
                    sufio.byId("SF_PSU_PAUSE_PROMPT").style.display = "block";
                }
            }
        }
    };
    
    superfish.b.activateHeader = function() {
        superfish.b.closePSU( sufio.byId('SF_SLIDE_UP_CLOSE'), 5, 0);
    };
    
    superfish.b.movePSU = function(node, btn, duration, top, up, bgPos, bounce) {
        superfish.b.preSlideUpOn = up + 2;
        top = superfish.publisher.fixSuPos(top);
        var prop = {
            node:  node,
            duration: duration,
            properties: {
                top: top
            },
            onEnd:  function(){
                btn.style.backgroundPosition = bgPos;                
                sufio.attr( btn, "up", up );
                sufio.attr( btn, "title", superfish.b.titles[(up == 0 ? 3 : up+1)] );
            }
        };
        if (bounce) {
            prop.easing = sufio.fx.easing.bounceOut; 
        }
        sufio.animateProperty(prop).play();
        sufio.byId("SF_PSU_PAUSE_PROMPT").style.display = "none";
    };

    superfish.b.hidePSU = function(){
        clearTimeout(superfish.b.tm);
        if ( superfish.b.preSlideUpOn ){
            sufio.byId( "SF_SLIDE_UP" ).style.display='none';
            superfish.b.preSlideUpOn = 0;
            sufio.attr( "SF_SLIDE_UP_CLOSE", "up", 0 );
        }
    };
    
    superfish.b.initPSU = function( item ){
        var su = sufio.byId( "SF_SLIDE_UP" );
        if( su ){
            document.body.removeChild( su );
        }
        su = sufio.place( superfish.b.createPSU(), sufio.body() );

        if (su) {
            su.innerHTML = su.innerHTML.replace( "{TITLE}", item.title);
            su.innerHTML = su.innerHTML.replace( "{PRICE}", item.price);
            su.innerHTML = su.innerHTML.replace( /{STORE}/g, item.merchantName );
            var im = sufio.byId("SF_PSU_IMG_OBJ");
            im.src = item.imagePath;

            for (var i = 1; i <= 3; i++) {
                im = sufio.byId("SF_SLIDE_UP_VISIT_" + i);
                im.setAttribute('href', item.merchantUrl);
            }
            var cpn = sufio.byId("SF_SLIDE_UP_CPN");
//            if (!item.cpnUrl || !item.cpnNum) {
//                item.cpnUrl = "http://www.superfish.com/";
//                item.cpnNum = 43;
//            }

            if (item.cpnUrl && item.cpnNum) {
                if (!superfish.b.suMerch) {
                    superfish.b.suMerch = item.merchantName;
                }
                cpn.setAttribute('href', item.cpnUrl);
                var st = item.merchantName.replace('...', '');
                var max = 14 - (item.cpnNum + "").length;
                if (st.length > max) {
                    st = st.substring(0, max) + "...";
                }
                su.innerHTML = su.innerHTML.replace("{STORE2}", st);
                su.innerHTML = su.innerHTML.replace("{NUM}", item.cpnNum);  
            }
            else {
                sufio.destroy(cpn);
            }

            var vp = sufio.window.getBox();
            var t = vp.h + 10;
            var l = vp.w - parseInt( su.style.width ) - 111;
            if (spsupport.p.isIEQ) {
                t = t + vp.t;
                l = l + vp.l;
            }
            sufio.style(
                su ,{
                    top : t + "px",
                    left : l + "px",
                    display : "block"
                });

            var t2 = superfish.publisher.fixSuPos(parseInt( su.style.top ) - parseInt( su.style.height ) - 10);
            sufio.animateProperty({
                node:  su,
                duration: 1900,
                easing: sufio.fx.easing.bounceOut,
                properties: {
                    top: t2
                }
            }).play();
            superfish.b.preSlideUpOn = 2;                
        
            su.onclick = function() {
                if (!superfish.b.psuActive) {
                    superfish.b.psuActive = 1;
                }
            };

            su.onmouseover = function() {
                if (!superfish.b.psuActive) {
                    clearTimeout(superfish.b.tm);
                }
            };

            su.onmouseout = superfish.b.setTimer;
        }
    };

    superfish.b.setTimer = function() {
        if (!superfish.b.psuActive) {
            superfish.b.tm = setTimeout(function() {
                superfish.b.psuActive = 1;
                var btn = sufio.byId( "SF_SLIDE_UP_CLOSE" );
                if (btn) {
                    superfish.b.closePSU(btn, 5, 1);
                }
            }, superfish.b.slideUpTime);
        }
    };
 
            
   
    superfish.b.psuGlide = function(){
        var su = superfish.util.bubble();
        var psu = superfish.util.preslideup();
        var vp = sufio.window.getBox();
        var t = vp.h + 4;
        var l = vp.w - superfish.p.width - 4;
        if (spsupport.p.isIEQ) { 
            t = t + vp.t;
            l = l + vp.l;
        }
        
        su.style.top = t + "px";
        su.style.left = l + "px";
        var step1 = superfish.publisher.fixSuPos(t - 4 - parseInt(psu.style.height) + spsupport.p.psuHdrHeight);        
       var step = parseInt( su.style.top ) - superfish.p.height - 10;

        sufio.byId("SF_PSU_PAUSE_PROMPT").style.display = "none";
        sufio.byId( "SF_SLIDE_UP_SLEEP" ).style.display = "none";
        sufio.animateProperty({
            node:  su,
            duration: 250,
            properties: {
                top: step1
            },
            onEnd: function() {
                superfish.b.movePSU(psu, sufio.byId("SF_SLIDE_UP_CLOSE"), 600, step - spsupport.p.psuHdrHeight, 1, "0px -22px", 0);
                sufio.animateProperty({
                    node:  su,
                    duration: 600,
                    properties: {
                        top: superfish.publisher.fixSuPos(step)
                    }
                }).play();
            }
        }).play();
        
        setTimeout( function(){
            spsupport.api.jsonpRequest(
                spsupport.p.sfDomain + "trackSession.action",
                {
                    "action" : "full slideup",
                    "userid" : spsupport.p.userid,
                    "sessionid" : superfish.util.currentSessionId
                } )
        }, 1500);
        superfish.b.slideUpOn = 1;
    };
            

    }
}
