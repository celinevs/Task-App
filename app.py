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
        urgent_tasks = Task.query.filter_by(type='urgent', status='in-progress')\
                               .order_by(Task.dateUpdated.desc())\
                               .limit(3)\
                               .all()
        
        # Initialize counts dictionary with all required categories set to 0
        counts_dict = {
            'important': 0,
            'regular': 0,
            'done': 0
        }
        
        # Get counts for important and regular types
        type_counts = db.session.query(
            Task.type,
            db.func.count(Task.id).label('count')
        ).filter(Task.type.in_(['important', 'regular']))\
         .group_by(Task.type).all()
        
        # Get count for done status
        done_count = db.session.query(
            db.func.count(Task.id)
        ).filter_by(status='done').scalar() or 0
        
        # Update counts dictionary
        for type, count in type_counts:
            counts_dict[type] = count
        counts_dict['done'] = done_count
        
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
    
@app.route('/api/tasks/all', methods=['GET'])
def get_all_tasks():
    try:
        # Get query parameters
        task_type = request.args.get('type', None)
        status = request.args.get('status', None)
        
        # Start with base query
        query = Task.query
        
        # Apply filters if provided
        if task_type:
            query = query.filter_by(type=task_type)
        if status:
            query = query.filter_by(status=status)
        
        # Execute query with sorting
        tasks = query.order_by(Task.dateUpdated.desc()).all()
        
        return jsonify({
            'success': True,
            'count': len(tasks),
            'tasks': [task.to_dict() for task in tasks]
        })
        
    except Exception as e:
        app.logger.error(f"Error fetching all tasks: {str(e)}")
        return jsonify({
            'success': False,
            'message': 'Failed to fetch tasks',
            'error': str(e)
        }), 500

    
@app.route('/api/tasks', methods=['POST'])
def create_task():
    data = request.get_json()
    new_task = Task(
        title=data['title'],
        description=data.get('description', ''),
        type=data['type'],
        status=data['status'],
        date=datetime.strptime(data['dateUpdated'], '%Y-%m-%d').date()
    )
    db.session.add(new_task)
    db.session.commit()
    return jsonify(new_task.to_dict()), 201

@app.route('/api/tasks/<int:task_id>', methods=['PUT'])
def update_task(task_id):
    task = Task.query.get_or_404(task_id)
    data = request.get_json()
    
    task.title = data['title']
    task.description = data.get('description', '')
    task.type = data['type']
    task.status = data['status']
    task.dateUpdated = datetime.strptime(data['dateUpdated'], '%Y-%m-%d').date()
    
    db.session.commit()
    return jsonify(task.to_dict())

@app.route('/api/tasks/<int:task_id>', methods=['DELETE'])
def delete_task(task_id):
    task = Task.query.get_or_404(task_id)
    db.session.delete(task)
    db.session.commit()
    return jsonify({'message': 'Task deleted'}), 200

if __name__ == '__main__':
    with app.app_context():
        try:
            db.create_all()
            print("Task table created successfully!")
        except Exception as e:
            print(f"Error creating table: {e}")
    app.run(host='0.0.0.0', port=5000, debug=True)