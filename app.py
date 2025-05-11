from flask import Flask, jsonify, request
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from datetime import datetime



app = Flask(__name__)
app.config.from_object(__name__)

CORS(app)

app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql://root@localhost:3308/task_manager'

app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)

# Define Transaction Model
class Task(db.Model):
    __tablename__ = 'task'

    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    title = db.Column(db.String(100), nullable=False)
    description = db.Column(db.String(255), nullable=True)
    type = db.Column(db.String(50), nullable=False)
    dateUpdated = db.Column(db.Date)
    status = db.Column(db.String(50), nullable=False)

    def __init__(self, title, date, description, type, status):
        self.title = title
        self.description = description
        self.type = type
        self.status = status
        self.dateUpdated = date

    def to_dict(self):
        """Converts a Task object to a dictionary for JSON serialization."""
        return {
        'id': self.id,
        'title': self.title,
        'description': self.description,
        'type': self.type,
        'status': self.status,
        'dateUpdated': self.dateUpdated.isoformat() if hasattr(self.dateUpdated, 'isoformat') else self.dateUpdated}



@app.route('/api/tasks', methods=['GET'])
def get_tasks():
    try:
        # Get 3 most recent urgent tasks
        urgent_tasks = Task.query.filter_by(type='urgent')\
                               .order_by(Task.dateUpdated.desc())\
                               .limit(3)\
                               .all()
        
        # Get counts by task type
        task_counts = db.session.query(
            Task.type,
            db.func.count(Task.id).label('count')
        ).group_by(Task.type).all()
        
        counts_dict = {task_type: count for task_type, count in task_counts}
        
        return jsonify({
            'urgent_tasks': [task.to_dict() for task in urgent_tasks],
            'task_counts': counts_dict,
        })
        
    except Exception as e:
        app.logger.error(f"Error fetching tasks: {str(e)}")
        return jsonify({
            'status': 'error',
            'message': 'Failed to fetch tasks'
        }), 500

if __name__ == '__main__':
    with app.app_context():
        try:
            db.create_all()
            print("Task table created successfully!")
        except Exception as e:
            print(f"Error creating table: {e}")
    app.run(host='0.0.0.0', port=5000, debug=True)