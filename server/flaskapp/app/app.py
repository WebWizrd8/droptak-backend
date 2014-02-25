from utils.path import fix_path
import os
from google.appengine.api.logservice import logservice
import logging
from User import Account 
from Tak import Tak 

# this fix allows us to import modues/packages found in 'lib'
fix_path(os.path.abspath(os.path.dirname(__file__)))

from flask import Flask, render_template, request
from blueprints.example.views import bp as example_blueprint


app = Flask(__name__)


@app.route('/')
def index():
    return render_template('index.html')

@app.route('/maps')
def maps():
    return render_template('map.html')
@app.route('/login',methods=['GET','POST'])
def login():
	if request.method == 'POST':
			name = request.args.get("name", "")
			email = request.args.get("email", "")
			# check if arg blank
			logging.info("Name %s" %name)
			logging.info("Email %s" %email)
			account  = Account(name=name,email=email)
			query = Account.query(Account.email == email)
			logging.info(query.count())
			if query.count() == 0:
				account.put()
			return '200'
	if request.method == 'GET':
		return page_not_found(404)


@app.route('/taks',methods=['GET','POST'])
def taks():
	if request.method == 'POST':
			lat = request.args.get("lat", "") 
			lng = request.args.get("lng", "") # 2nd arg is default
			user = request.args.get("user", "") # 2nd arg is default
			# check if args blank
			logging.info("Add lat %s, lng %s" %(lat, lng) )
			tak  = Tak(lng=lng,lat=lat, creator=user)
			tak.put()
			return '200'
	if request.method == 'GET':
		return render_template('taks.html')

@app.errorhandler(404)
def page_not_found(e):
    return '404: Page Not Found'


# register Blueprints
app.register_blueprint(example_blueprint)
