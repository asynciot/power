webpackJsonp([42],{"0jjb":function(e,t){},"1/1N":function(e,t,n){"use strict";var a=n("JtY9"),o=(n.n(a),n("b71I"));n.n(o)},"7ZAf":function(e,t,n){"use strict";n.d(t,"a",function(){return k});var a=(n("1/1N"),n("pU9O")),o=n("YbOa"),i=n.n(o),r=n("U5hO"),l=n.n(r),u=n("EE81"),c=n.n(u),s=n("Jmyu"),f=n.n(s),d=n("/00i"),p=n.n(d),m=n("9wvh"),v=n.n(m),h=n("A1Y1"),y=(n.n(h),n("0M1p")),b=n.n(y),_=n("ZQJc"),E=n.n(_),g=n("FQ8f"),C=n.n(g),O=function(e,t){var n=b()("/:name/:sec?/(.*)?").exec(e);return"list"===n[2]?t===n[3]?C.a.active:"":"radar"===n[1]?t===n[2]?C.a.active:"":"tech"===n[1]?t===n[2]?C.a.active:"":"statistics"===n[2]?t===n[3]?C.a.active:"":void 0},k=function(e){function t(){return i()(this,t),f()(this,p()(t).apply(this,arguments))}return c()(t,[{key:"render",value:function(){var e=this.props,t=e.navs,n=e.active,o=t.map(function(e){return v.a.createElement(h.Link,{className:E()(C.a.nav,O(e.link,n)),to:e.link,key:e.label},v.a.createElement("p",null,e.icon&&v.a.createElement(a.a,{className:C.a.icon,type:e.icon}),e.label,"\xa0",null==e.num?"":"( ".concat(e.num," )")))});return v.a.createElement("div",{className:C.a.menu},o)}}]),l()(t,e),t}(m.PureComponent)},"8LWd":function(e,t,n){"use strict";function a(e){return e&&e.__esModule?e:{default:e}}Object.defineProperty(t,"__esModule",{value:!0});var o=n("4YfN"),i=a(o),r=n("AA3o"),l=a(r),u=n("xSur"),c=a(u),s=n("UzKs"),f=a(s),d=n("Y7Ml"),p=a(d),m=n("ZQJc"),v=a(m),h=n("9wvh"),y=a(h),b=n("i0Sd"),_=a(b),E=function(e,t){var n={};for(var a in e)Object.prototype.hasOwnProperty.call(e,a)&&t.indexOf(a)<0&&(n[a]=e[a]);if(null!=e&&"function"==typeof Object.getOwnPropertySymbols)for(var o=0,a=Object.getOwnPropertySymbols(e);o<a.length;o++)t.indexOf(a[o])<0&&(n[a[o]]=e[a[o]]);return n},g=function(e){function t(){return(0,l.default)(this,t),(0,f.default)(this,(t.__proto__||Object.getPrototypeOf(t)).apply(this,arguments))}return(0,p.default)(t,e),(0,c.default)(t,[{key:"render",value:function(){var e=this.props,t=e.prefixCls,n=e.children,a=e.className,o=e.style,r=e.renderHeader,l=e.renderFooter,u=E(e,["prefixCls","children","className","style","renderHeader","renderFooter"]),c=(0,v.default)(t,a);return y.default.createElement("div",(0,i.default)({className:c,style:o},u),r?y.default.createElement("div",{className:t+"-header"},"function"==typeof r?r():r):null,n?y.default.createElement("div",{className:t+"-body"},n):null,l?y.default.createElement("div",{className:t+"-footer"},"function"==typeof l?l():l):null)}}]),t}(y.default.Component);t.default=g,g.Item=_.default,g.defaultProps={prefixCls:"am-list"},e.exports=t.default},CHpA:function(e,t){},FQ8f:function(e,t){e.exports={menu:"menu___qTgP-",nav:"nav___2XS4B",icon:"icon___DhtCy",active:"active___6L2K0"}},OBsF:function(e,t){},SVLV:function(e,t,n){"use strict";n("l/Gp"),n("0jjb")},aMf9:function(e,t,n){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),n.d(t,"default",function(){return N});var a,o,i=n("SVLV"),r=(n.n(i),n("8LWd")),l=n.n(r),u=n("YbOa"),c=n.n(u),s=n("U5hO"),f=n.n(s),d=n("EE81"),p=n.n(d),m=n("Jmyu"),v=n.n(m),h=n("/00i"),y=n.n(h),b=n("9wvh"),_=n.n(b),E=n("NmwX"),g=(n.n(E),n("0M1p")),C=n.n(g),O=n("wAcL"),k=n.n(O),w=n("7ZAf"),M=[{label:"\u5bb6\u7528\u68af\u914d\u5957\u7cfb\u5217\u6307\u5357",link:"FAMILY_LAD"}],N=(a=Object(E.connect)(function(e){return{tech:e.tech}}))(o=function(e){function t(){var e,n,a;c()(this,t);for(var o=arguments.length,i=new Array(o),r=0;r<o;r++)i[r]=arguments[r];return v()(a,(n=a=v()(this,(e=y()(t)).call.apply(e,[this].concat(i))),a.state={navs:[{label:"\u4ea7\u54c1\u8bf4\u660e\u4e66",link:"/tech/manual"},{label:"\u5176\u4ed6\u76f8\u5173\u8d44\u6599",link:"/tech/other"}]},a.onClick=function(e){a.props.history.push("/tech/reader/".concat(e))},n))}return p()(t,[{key:"componentDidMount",value:function(){}},{key:"render",value:function(){var e=this,t=this.props.location,n=C()("/tech/:name").exec(t.pathname),a=n[1];return _.a.createElement("div",{className:"content"},_.a.createElement(w.a,{key:"nav",active:a,navs:this.state.navs}),_.a.createElement("div",{className:k.a.content},_.a.createElement(l.a,{style:{backgroundColor:"white"}},M.map(function(t){return _.a.createElement(l.a.Item,{key:t.label,onClick:function(){return e.onClick(t.link)},arrow:"horizontal"},t.label,"\u95e8\u63a7\u5236\u5668\u8bf4\u660e\u4e66")}))))}}]),f()(t,e),t}(b.Component))||o},b71I:function(e,t){},i0Sd:function(e,t,n){"use strict";function a(e){return e&&e.__esModule?e:{default:e}}Object.defineProperty(t,"__esModule",{value:!0}),t.Brief=void 0;var o=n("4YfN"),i=a(o),r=n("a3Yh"),l=a(r),u=n("AA3o"),c=a(u),s=n("xSur"),f=a(s),d=n("UzKs"),p=a(d),m=n("Y7Ml"),v=a(m),h=n("ZQJc"),y=a(h),b=n("9wvh"),_=a(b),E=n("v39Q"),g=a(E),C=function(e,t){var n={};for(var a in e)Object.prototype.hasOwnProperty.call(e,a)&&t.indexOf(a)<0&&(n[a]=e[a]);if(null!=e&&"function"==typeof Object.getOwnPropertySymbols)for(var o=0,a=Object.getOwnPropertySymbols(e);o<a.length;o++)t.indexOf(a[o])<0&&(n[a[o]]=e[a[o]]);return n},O=t.Brief=function(e){function t(){return(0,c.default)(this,t),(0,p.default)(this,(t.__proto__||Object.getPrototypeOf(t)).apply(this,arguments))}return(0,v.default)(t,e),(0,f.default)(t,[{key:"render",value:function(){return _.default.createElement("div",{className:"am-list-brief",style:this.props.style},this.props.children)}}]),t}(_.default.Component),k=function(e){function t(e){(0,c.default)(this,t);var n=(0,p.default)(this,(t.__proto__||Object.getPrototypeOf(t)).call(this,e));return n.onClick=function(e){var t=n.props,a=t.onClick,o=t.platform,i="android"===o;if(a&&i){n.debounceTimeout&&(clearTimeout(n.debounceTimeout),n.debounceTimeout=null);var r=e.currentTarget,l=Math.max(r.offsetHeight,r.offsetWidth),u=e.currentTarget.getBoundingClientRect(),c=e.clientX-u.left-r.offsetWidth/2,s=e.clientY-u.top-r.offsetWidth/2,f={width:l+"px",height:l+"px",left:c+"px",top:s+"px"};n.setState({coverRippleStyle:f,RippleClicked:!0},function(){n.debounceTimeout=setTimeout(function(){n.setState({coverRippleStyle:{display:"none"},RippleClicked:!1})},1e3)})}a&&a(e)},n.state={coverRippleStyle:{display:"none"},RippleClicked:!1},n}return(0,v.default)(t,e),(0,f.default)(t,[{key:"componentWillUnmount",value:function(){this.debounceTimeout&&(clearTimeout(this.debounceTimeout),this.debounceTimeout=null)}},{key:"render",value:function(){var e,t,n,a=this,o=this.props,r=o.prefixCls,u=o.className,c=o.activeStyle,s=o.error,f=o.align,d=o.wrap,p=o.disabled,m=o.children,v=o.multipleLine,h=o.thumb,b=o.extra,E=o.arrow,O=o.onClick,k=C(o,["prefixCls","className","activeStyle","error","align","wrap","disabled","children","multipleLine","thumb","extra","arrow","onClick"]),w=(k.platform,C(k,["platform"])),M=this.state,N=M.coverRippleStyle,T=M.RippleClicked,S=(0,y.default)(r+"-item",u,(e={},(0,l.default)(e,r+"-item-disabled",p),(0,l.default)(e,r+"-item-error",s),(0,l.default)(e,r+"-item-top","top"===f),(0,l.default)(e,r+"-item-middle","middle"===f),(0,l.default)(e,r+"-item-bottom","bottom"===f),e)),x=(0,y.default)(r+"-ripple",(0,l.default)({},r+"-ripple-animate",T)),j=(0,y.default)(r+"-line",(t={},(0,l.default)(t,r+"-line-multiple",v),(0,l.default)(t,r+"-line-wrap",d),t)),P=(0,y.default)(r+"-arrow",(n={},(0,l.default)(n,r+"-arrow-horizontal","horizontal"===E),(0,l.default)(n,r+"-arrow-vertical","down"===E||"up"===E),(0,l.default)(n,r+"-arrow-vertical-up","up"===E),n)),A=_.default.createElement("div",(0,i.default)({},w,{onClick:function(e){a.onClick(e)},className:S}),h?_.default.createElement("div",{className:r+"-thumb"},"string"==typeof h?_.default.createElement("img",{src:h}):h):null,_.default.createElement("div",{className:j},void 0!==m&&_.default.createElement("div",{className:r+"-content"},m),void 0!==b&&_.default.createElement("div",{className:r+"-extra"},b),E&&_.default.createElement("div",{className:P,"aria-hidden":"true"})),_.default.createElement("div",{style:N,className:x})),L={};return Object.keys(w).forEach(function(e){/onTouch/i.test(e)&&(L[e]=w[e],delete w[e])}),_.default.createElement(g.default,(0,i.default)({},L,{disabled:p||!O,activeStyle:c,activeClassName:r+"-item-active"}),A)}}]),t}(_.default.Component);k.defaultProps={prefixCls:"am-list",align:"middle",error:!1,multipleLine:!1,wrap:!1,platform:"ios"},k.Brief=O,t.default=k},"l/Gp":function(e,t,n){"use strict";n("OBsF"),n("CHpA")},v39Q:function(e,t,n){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var a=n("4YfN"),o=n.n(a),i=n("AA3o"),r=n.n(i),l=n("xSur"),u=n.n(l),c=n("UzKs"),s=n.n(c),f=n("Y7Ml"),d=n.n(f),p=n("9wvh"),m=n.n(p),v=n("ZQJc"),h=n.n(v),y=function(e){function t(){r()(this,t);var e=s()(this,(t.__proto__||Object.getPrototypeOf(t)).apply(this,arguments));return e.state={active:!1},e.onTouchStart=function(t){e.triggerEvent("TouchStart",!0,t)},e.onTouchMove=function(t){e.triggerEvent("TouchMove",!1,t)},e.onTouchEnd=function(t){e.triggerEvent("TouchEnd",!1,t)},e.onTouchCancel=function(t){e.triggerEvent("TouchCancel",!1,t)},e.onMouseDown=function(t){e.triggerEvent("MouseDown",!0,t)},e.onMouseUp=function(t){e.triggerEvent("MouseUp",!1,t)},e.onMouseLeave=function(t){e.triggerEvent("MouseLeave",!1,t)},e}return d()(t,e),u()(t,[{key:"componentDidUpdate",value:function(){this.props.disabled&&this.state.active&&this.setState({active:!1})}},{key:"triggerEvent",value:function(e,t,n){var a="on"+e,o=this.props.children;o.props[a]&&o.props[a](n),t!==this.state.active&&this.setState({active:t})}},{key:"render",value:function(){var e=this.props,t=e.children,n=e.disabled,a=e.activeClassName,i=e.activeStyle,r=n?void 0:{onTouchStart:this.onTouchStart,onTouchMove:this.onTouchMove,onTouchEnd:this.onTouchEnd,onTouchCancel:this.onTouchCancel,onMouseDown:this.onMouseDown,onMouseUp:this.onMouseUp,onMouseLeave:this.onMouseLeave},l=m.a.Children.only(t);if(!n&&this.state.active){var u=l.props,c=u.style,s=u.className;return!1!==i&&(i&&(c=o()({},c,i)),s=h()(s,a)),m.a.cloneElement(l,o()({className:s,style:c},r))}return m.a.cloneElement(l,r)}}]),t}(m.a.Component),b=y;y.defaultProps={disabled:!1},n.d(t,"default",function(){return b})},wAcL:function(e,t){e.exports={content:"content___1EtA0"}}});